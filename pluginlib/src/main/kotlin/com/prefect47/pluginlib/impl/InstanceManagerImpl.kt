/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 Niklas Brunlid
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.prefect47.pluginlib.impl

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prefect47.pluginlib.impl.InstanceManager.PluginInfo
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginListener
import com.prefect47.pluginlib.impl.VersionInfo.InvalidVersionException
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.reflect.full.createInstance

class InstanceManagerImpl<T: Plugin>(
    private val context: Context, private val manager: Manager, private val pluginPrefs: PluginPrefs,
    private val control: PluginLibraryControl, private val action: String, private val listener: PluginListener<T>?,
    private val allowMultiple: Boolean, private val version: VersionInfo
): InstanceManager<T> {

    class Factory @Inject constructor(
        private val context: Context, private val manager: Manager, private val control: PluginLibraryControl,
        private val pluginPrefs: PluginPrefs
    ): InstanceManager.Factory {

        override fun <T: Plugin> create(action: String, listener: PluginListener<T>?,
                allowMultiple: Boolean, cls: KClass<*>) = InstanceManagerImpl(
            context,
            manager,
            pluginPrefs,
            control,
            action,
            listener,
            allowMultiple,
            VersionInfo().addClass(cls)
        )
    }

    companion object {
        private const val TAG = "InstanceManager"
    }

    private val pluginHandler = PluginHandler()
    private val pm = context.packageManager

    private val notificationId = Manager.nextNotificationId

    override fun getPlugin(): PluginInfo<T>? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("Must be called from UI thread")
        }
        runBlocking {
            pluginHandler.handleQueryPlugins( notify = false ) // Don't call listeners
        }
        if (pluginHandler.plugins.size > 0) {
            val info = pluginHandler.plugins[0]
            pluginPrefs.setHasPlugins()
            info.plugin.onCreate()
            return info
        }
        return null
    }

    override suspend fun loadAll() {
        if (control.debugEnabled) Log.d(TAG, "loadAll")
        pluginHandler.queryAll()
    }

    override fun destroy() {
        if (control.debugEnabled) Log.d(TAG, "destroy")
        val plugins = ArrayList<PluginInfo<T>>(pluginHandler.plugins)
        plugins.forEach {
            pluginHandler.handlePluginDisconnected(it)
        }
    }

    override fun onPackageRemoved(pkg: String) {
        pluginHandler.removePackage(pkg)
    }

    override fun onPackageChange(pkg: String) {
        pluginHandler.removePackage(pkg)
        pluginHandler.queryPackage(pkg)
    }

    override fun checkAndDisable(className: String): Boolean {
        var disableAny = false
        val plugins = ArrayList<PluginInfo<T>>(pluginHandler.plugins)
        plugins.forEach {
            if (className.startsWith(it.pkg)) {
                disable(it)
                disableAny = true
            }
        }
        return disableAny
    }

    override fun disableAll(): Boolean {
        val plugins = ArrayList<PluginInfo<T>>(pluginHandler.plugins)
        plugins.forEach {
            disable(it)
        }
        return plugins.size != 0
    }

    private fun disable(info: PluginInfo<*>) {
        // Live by the sword, die by the sword.
        // Misbehaving plugins get disabled and won't come back until uninstall/reinstall.

        // If a plugin is detected in the stack of a crash then this will be called for that
        // plugin, if the plugin causing a crash cannot be identified, they are all disabled
        // assuming one of them must be bad.
        Log.w(TAG, "Disabling plugin ${info.pkg}/${info.plugin::class.qualifiedName}")
        pm.setComponentEnabledSetting(
                ComponentName(info.pkg, info.plugin.className),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }

    override fun <P: Any> dependsOn(p: Plugin, cls: KClass<P>): Boolean {
        val plugins = ArrayList<PluginInfo<T>>(pluginHandler.plugins)
        plugins.forEach {
            if (it.plugin::class.simpleName.equals(p::class.simpleName)) {
                return (it.version != null && it.version.hasClass(cls))
            }
        }
        return false
    }

    override fun toString(): String {
        return String.format("%s@%s (action=%s)", this::class.simpleName, hashCode(), action)
    }

    inner class PluginHandler: CoroutineScope {
        val plugins = ArrayList<PluginInfo<T>>()

        override val coroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

        suspend fun queryAll() {
            if (control.debugEnabled) Log.d(TAG, "queryAll $action")
            plugins.forEach {
                listener!!.onPluginDisconnected(it.plugin)
                //if (!(it.plugin is PluginFragment)) {
                    // Only call onDestroy for plugins that aren't fragments, as fragments
                    // will get the onDestroy as part of the fragment lifecycle.
                    it.plugin.onDestroy()
                //}
                manager.pluginInfoMap.remove(it.plugin)
            }
            plugins.clear()
            handleQueryPlugins()
        }

        fun removePackage(pkg: String) {
            launch {
                plugins.forEach {
                    if (it.pkg == pkg) {
                        handlePluginDisconnected(it)
                        plugins.remove(it)
                    }
                }
            }
        }

        fun queryPackage(pkg: String) {
            launch {
                if (control.debugEnabled) Log.d(TAG, "queryPkg $pkg $action")
                if (allowMultiple || (plugins.size == 0)) {
                    handleQueryPlugins(pkg)
                } else {
                    if (control.debugEnabled) Log.d(TAG, "Too many of $action")
                }
            }
        }

        private fun handlePluginConnected(info: PluginInfo<T>) {
            pluginPrefs.setHasPlugins()
            launch(Dispatchers.Main) {
                manager.handleWtfs()
                manager.pluginInfoMap[info.plugin] = info

                //if (!(msg.obj is PluginFragment)) {
                    // Only call onCreate for plugins that aren't fragments, as fragments
                    // will get the onCreate as part of the fragment lifecycle.
                    info.plugin.onCreate()
                //}
                listener!!.onPluginConnected(info.plugin)
            }
        }

        fun handlePluginDisconnected(info: PluginInfo<T>) {
            launch(Dispatchers.Main) {
                if (control.debugEnabled) Log.d(TAG, "onPluginDisconnected")
                listener!!.onPluginDisconnected(info.plugin)
                //if (!(msg.obj is PluginFragment)) {
                    // Only call onDestroy for plugins that aren't fragments, as fragments
                    // will get the onDestroy as part of the fragment lifecycle.
                    info.plugin.onDestroy()
                //}
                manager.pluginInfoMap.remove(info.plugin)
            }
        }

        suspend fun handleQueryPlugins(pkgName: String? = null, notify: Boolean = true) {
            // This isn't actually a service and shouldn't ever be started, but is
            // a convenient PM based way to manage our plugins.
            val intent = Intent(action)
            if (pkgName != null) {
                intent.setPackage(pkgName)
            }
            val result: MutableList<ResolveInfo> = pm.queryIntentServices(intent, 0)
            if (control.debugEnabled) Log.d(TAG, "Found ${result.size} plugins for $action")
            if (result.size > 1 && !allowMultiple) {
                // TODO: Show warning.
                Log.w(TAG, "Multiple plugins found for $action")
                return
            }

            coroutineScope {
                result.forEach {
                    launch(coroutineContext) {
                        val name = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
                        handleLoadPlugin(name)?.let { info ->
                            if (notify) {
                                handlePluginConnected(info)
                            }
                            plugins.add(info)
                        }
                    }
                }
            }
        }

        private fun handleLoadPlugin(component: ComponentName): PluginInfo<T>? {
            val pkg = component.packageName
            val cls = component.className
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                // TODO: This probably isn't needed given that we don't have IGNORE_SECURITY on
                val permissionName = control.permissionName
                if (pm.checkPermission(permissionName, pkg) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Plugin doesn't have permission: $pkg")
                    return null
                }
                // Create our own ClassLoader so we can use our own code as the parent.
                val classLoader = manager.getClassLoader(info.sourceDir, info.packageName)
                val pluginContext =
                    PluginContextWrapper(context, context.createPackageContext(pkg, 0), classLoader, pkg)

                // TODO: Can we work around this without suppressing the warning?
                @Suppress("UNCHECKED_CAST")
                val pluginClass = Class.forName(cls, true, classLoader).kotlin as KClass<T>

                // TODO: Only create the plugin before version check if we need it for legacy version check.

                var pluginVersion: VersionInfo? = null
                try {
                    pluginVersion = checkVersion(pluginClass, version)
                    //val pluginVersion: VersionInfo? = checkVersion(pluginClass, plugin, version)
                    if (control.debugEnabled) Log.d(TAG, "createPlugin")

                    // We support object/singleton plugins as well
                    //@Suppress("UNCHECKED_CAST")
                    //val pluginClass = Class.forName(cls, false, classLoader).kotlin as KClass<T>
                    val plugin: T = pluginClass.objectInstance ?: pluginClass.createInstance()
                    //val plugin = pluginClass.createInstance()

                    return PluginInfo(plugin, pkg, pluginVersion, pluginContext)
                } catch (e: InvalidVersionException) {
                    notifyInvalidVersion(component, cls, e.tooNew, e.message)
                    // TODO: Warn user.
                    //@Suppress("DEPRECATION")
                    Log.w(TAG, "Plugin $cls has invalid interface $pluginVersion, expected $version")
                    return null
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Couldn't load plugin: $pkg", e)
                return null
            }
        }

        private fun notifyInvalidVersion(component: ComponentName, cls: String, tooNew: Boolean, msg: String?) {
            control.notificationChannel?.let { channel ->
                val color = Resources.getSystem().getIdentifier(
                    "system_notification_accent_color", "color", "android"
                )
                val nb = NotificationCompat.Builder(context, channel)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setWhen(0)
                    .setShowWhen(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(context.getColor(color))

                control.notificationIconResId.let {
                    if (it != 0) nb.setSmallIcon(it)
                }

                var label: String = cls
                try {
                    label = pm.getServiceInfo(component, 0).loadLabel(pm).toString()
                } catch (e2: NameNotFoundException) {
                }
                if (!tooNew) {
                    nb.setContentTitle("Plugin \"$label\" is too old")
                        .setContentText("Contact plugin developer to get an updated version.\n$msg")
                } else {
                    nb.setContentTitle("Plugin \"$label\" is too new")
                        .setContentText("Check to see if an OTA is available.\n$msg")
                }
                val i: Intent = Intent(Manager.DISABLE_PLUGIN).setData(
                    Uri.parse("package://" + component.flattenToString())
                )
                val pi: PendingIntent = PendingIntent.getBroadcast(context, 0, i, 0)
                nb.addAction(NotificationCompat.Action(0, "Disable plugin", pi))
                NotificationManagerCompat.from(context).notify(notificationId, nb.build())
            }
        }

        @Throws(InvalidVersionException::class)
        private fun checkVersion(pluginClass: KClass<*>, version: VersionInfo): VersionInfo? {
            val pv: VersionInfo = VersionInfo().addClass(pluginClass)
            if (pv.hasVersionInfo()) {
                version.checkVersion(pv)
            } else {
                throw InvalidVersionException("Invalid version, investigate!", false)
                /*
                @Suppress("DEPRECATION")
                val fallbackVersion = plugin.version
                if (fallbackVersion != version.getDefaultVersion()) {
                    throw InvalidVersionException("Invalid legacy version", false)
                }
                return null
                */
            }
            return pv
        }
    }
}
