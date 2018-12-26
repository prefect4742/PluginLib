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

import android.app.Notification
import android.app.Notification.Action
import android.app.NotificationManager
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
import com.prefect47.pluginlib.impl.PluginInstanceManager.PluginInfo
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginListener
import com.prefect47.pluginlib.impl.VersionInfo.InvalidVersionException
import java.util.ArrayList
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlinx.coroutines.*
import kotlin.reflect.full.createInstance

class PluginInstanceManagerImpl<T: Plugin>(
    val context: Context, val action: String, val listener: PluginListener<T>?, val allowMultiple: Boolean,
    val version: VersionInfo, val manager: PluginManager
): PluginInstanceManager<T> {

    companion object factory: PluginInstanceManager.Factory {
        private const val DEBUG = false
        private const val TAG = "PluginInstanceManager"

        override fun <T: Plugin> create(context: Context, action: String, listener: PluginListener<T>?,
                allowMultiple: Boolean, cls: KClass<*>,
                manager: PluginManager
        ): PluginInstanceManager<T> {
            return PluginInstanceManagerImpl(
                context,
                action,
                listener,
                allowMultiple,
                VersionInfo().addClass(cls),
                manager
            )
        }
    }

    private val pluginHandler = PluginHandler()
    private val pm = context.packageManager

    private val notificationId = PluginManager.nextNotificationId

    override fun getPlugin(): PluginInfo<T>? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("Must be called from UI thread")
        }
        pluginHandler.handleQueryPlugins( notify = false ) // Don't call listeners
        if (pluginHandler.plugins.size > 0) {
            val info = pluginHandler.plugins[0]
            Dependency[PluginPrefs::class].setHasPlugins()
            info.plugin.onCreate(context, info.pluginContext)
            return info
        }
        return null
    }

    override fun loadAll() {
        if (DEBUG) Log.d(TAG, "loadAll")
        pluginHandler.queryAll()
    }

    override fun destroy() {
        if (DEBUG) Log.d(TAG, "destroy")
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
        Log.w(TAG, "Disabling plugin " + info.pkg + "/" + info.cls)
        pm.setComponentEnabledSetting(
                ComponentName(info.pkg, info.cls),
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

        override val coroutineContext = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        fun queryAll() {
            launch {
                if (DEBUG) Log.d(TAG, "queryAll $action")
                plugins.forEach {
                    listener!!.onPluginDisconnected(it.plugin)
                    //if (!(it.plugin is PluginFragment)) {
                        // Only call onDestroy for plugins that aren't fragments, as fragments
                        // will get the onDestroy as part of the fragment lifecycle.
                        it.plugin.onDestroy()
                    //}
                }
                plugins.clear()
                handleQueryPlugins()
            }
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
                if (DEBUG) Log.d(TAG, "queryPkg $pkg $action")
                if (allowMultiple || (plugins.size == 0)) {
                    handleQueryPlugins(pkg)
                } else {
                    if (DEBUG) Log.d(TAG, "Too many of $action")
                }
            }
        }

        fun handlePluginConnected(info: PluginInfo<T>) {
            launch(Dispatchers.Main) {
                Dependency[PluginPrefs::class].setHasPlugins()
                val descr = Dependency[PluginMetadataFactory::class].create(context, info.pluginContext, info.cls)
                manager.handleWtfs()
                //if (!(msg.obj is PluginFragment)) {
                    // Only call onCreate for plugins that aren't fragments, as fragments
                    // will get the onCreate as part of the fragment lifecycle.
                    info.plugin.onCreate(context, info.pluginContext)
                //}
                listener!!.onPluginConnected(info.plugin, info.pluginContext, descr)
            }
        }

        fun handlePluginDisconnected(info: PluginInfo<T>) {
            launch(Dispatchers.Main) {
                if (DEBUG) Log.d(TAG, "onPluginDisconnected")
                listener!!.onPluginDisconnected(info.plugin)
                //if (!(msg.obj is PluginFragment)) {
                    // Only call onDestroy for plugins that aren't fragments, as fragments
                    // will get the onDestroy as part of the fragment lifecycle.
                    info.plugin.onDestroy()
                //}
            }
        }

        fun handleQueryPlugins(pkgName: String? = null, notify: Boolean = true) {
            // This isn't actually a service and shouldn't ever be started, but is
            // a convenient PM based way to manage our plugins.
            val intent = Intent(action)
            if (pkgName != null) {
                intent.setPackage(pkgName)
            }
            val result: MutableList<ResolveInfo> = pm.queryIntentServices(intent, 0)
            if (DEBUG) Log.d(TAG, "Found " + result.size + "com/prefect47/pluginlib/plugin")
            if (result.size > 1 && !allowMultiple) {
                // TODO: Show warning.
                Log.w(TAG, "Multiple plugins found for $action")
                return
            }
            result.forEach {
                val name = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
                val info = handleLoadPlugin(name) ?: return@forEach
                notify.let { handlePluginConnected(info) }
                plugins.add(info)
            }
        }

        private fun handleLoadPlugin(component: ComponentName): PluginInfo<T>? {
            val pkg = component.packageName
            val cls = component.className
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                // TODO: This probably isn't needed given that we don't have IGNORE_SECURITY on
                if (pm.checkPermission(PluginManager.PLUGIN_PERMISSION, pkg) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Plugin doesn't have permission: $pkg")
                    return null
                }
                // Create our own ClassLoader so we can use our own code as the parent.
                val classLoader = manager.getClassLoader(info.sourceDir, info.packageName)
                val pluginContext =
                    PluginContextWrapper(context.createPackageContext(pkg, 0), classLoader)

                // TODO: Can we work around this without suppressing the warning?
                @Suppress("UNCHECKED_CAST")
                //val pluginClass = classLoader.loadClass(cls).kotlin as KClass<T>
                //pluginClass.java.
                val pluginClass = Class.forName(cls, true, classLoader).kotlin as KClass<T>
                //val pluginClassInited = Class.forName(cls, true, classLoader).kotlin as KClass<T>

                // TODO: Only create the plugin before version check if we need it for legacy version check.

                try {
                    val pluginVersion: VersionInfo? = checkVersion(pluginClass, version)
                    //val pluginVersion: VersionInfo? = checkVersion(pluginClass, plugin, version)
                    if (DEBUG) Log.d(TAG, "createPlugin")

                    // We support object/singleton plugins as well
                    //@Suppress("UNCHECKED_CAST")
                    //val pluginClass = Class.forName(cls, false, classLoader).kotlin as KClass<T>
                    val plugin: T = pluginClass.objectInstance ?: pluginClass.createInstance()
                    //val plugin = pluginClass.createInstance()

                    return PluginInfo(pkg, cls, plugin, pluginContext, pluginVersion)
                } catch (e: InvalidVersionException) {
                    notifyInvalidVersion(component, cls, e.tooNew, e.message)
                    // TODO: Warn user.
                    //@Suppress("DEPRECATION")
                    //Log.w(TAG, "Plugin has invalid interface version " + plugin.version
                    //        + ", expected " + version)
                    Log.w(TAG, "Plugin has invalid interface version, expected $version")
                    return null
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Couldn't load plugin: $pkg", e)
                return null
            }
        }

        private fun notifyInvalidVersion(component: ComponentName, cls: String, tooNew: Boolean, msg: String?) {
            val icon = context.resources.getIdentifier("tuner", "drawable",
                    context.packageName)
            val color = Resources.getSystem().getIdentifier(
                    "system_notification_accent_color", "color", "android")
            val nb: Notification.Builder = Notification.Builder(context, PluginManager.NOTIFICATION_CHANNEL_ID)
                            .setStyle(Notification.BigTextStyle())
                            .setSmallIcon(icon)
                            .setWhen(0)
                            .setShowWhen(false)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setColor(context.getColor(color))
            var label: String = cls
            try {
                label = pm.getServiceInfo(component, 0).loadLabel(pm).toString()
            } catch (e2: NameNotFoundException) {
            }
            if (!tooNew) {
                // Localization not required as this will never ever appear in a user build.
                nb.setContentTitle("Plugin \"$label\" is too old")
                        .setContentText("Contact plugin developer to get an updated version.\n$msg")
            } else {
                // Localization not required as this will never ever appear in a user build.
                nb.setContentTitle("Plugin \"$label\" is too new")
                        .setContentText("Check to see if an OTA is available.\n$msg")
            }
            val i: Intent = Intent(PluginManager.DISABLE_PLUGIN).setData(
                    Uri.parse("package://" + component.flattenToString()))
            val pi: PendingIntent = PendingIntent.getBroadcast(context, 0, i, 0)
            nb.addAction(Action.Builder(null, "Disable plugin", pi).build())
            context.getSystemService(NotificationManager::class.java).notify(notificationId, nb.build())
        }

        @Throws(InvalidVersionException::class)
        private fun checkVersion(pluginClass: KClass<*>, version: VersionInfo): VersionInfo? {
        //private fun checkVersion(pluginClass: KClass<*>, plugin: T, version: VersionInfo): VersionInfo? {
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