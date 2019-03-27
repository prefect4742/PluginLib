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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prefect47.pluginlib.*
import com.prefect47.pluginlib.impl.VersionInfo.InvalidVersionException
import com.prefect47.pluginlib.DiscoverableInfo.Listener
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.reflect.KClass

class DiscoverableManagerImpl<T: Discoverable, I: DiscoverableInfo>(
    private val context: Context, private val manager: Manager, private val discoverablePrefs: DiscoverablePrefs,
    private val control: Control, private val discoverableInfoFactory: DiscoverableInfo.Factory<I>,
    private val factoryManager: FactoryManager,
    override val cls: KClass<out T>, private val action: String, private val listener: Listener<I>?,
    private val allowMultiple: Boolean
): DiscoverableManager<T, I> {

    class Factory @Inject constructor(
        private val context: Context, private val manager: Manager, private val control: Control,
        private val factoryManager: FactoryManager, private val discoverablePrefs: DiscoverablePrefs
    ): DiscoverableManager.Factory {

        override fun <T: Discoverable, I: DiscoverableInfo> create(
            action: String, listener: Listener<I>?, allowMultiple: Boolean, cls: KClass<out T>,
            discoverableInfoFactory: DiscoverableInfo.Factory<I>
        ): DiscoverableManager<T, I> {
            return DiscoverableManagerImpl(
                context,
                manager,
                discoverablePrefs,
                control,
                discoverableInfoFactory,
                factoryManager,
                cls,
                action,
                listener,
                allowMultiple
            )
        }
    }

    companion object {
        private const val TAG = "DiscoverableManager"
    }

    private val version = VersionInfo(control, factoryManager).addClass(cls)
    override val flags = findFlags(cls)
    override val discoverables: MutableList<I> = Collections.synchronizedList(ArrayList())

    private val pluginHandler = PluginHandler()
    private val pm = context.packageManager

    private val notificationId = Manager.nextNotificationId

    private val versionMap = HashMap<I, VersionInfo>()

    /*
    override fun getPlugin(): DiscoverableInfo<T>? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("Must be called from UI thread")
        }
        runBlocking {Discoverable
            pluginHandler.handleQueryPlugins( notify = false ) // Don't call listeners
        }
        if (discoverables.size > 0) {
            val info = discoverables[0]
            discoverablePrefs.setHasPlugins()
            info.plugin.onCreate()
            return info
        }
        return null
    }
    */

    override suspend fun loadAll() {
        if (control.debugEnabled) Log.d(TAG, "loadAll")
        pluginHandler.queryAll()
    }

    override fun destroy() {
        if (control.debugEnabled) Log.d(TAG, "destroy")
        ArrayList(discoverables).forEach {
            pluginHandler.handlePluginRemoved(it)
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
        ArrayList(discoverables).forEach {
            if (className.startsWith(it.component.packageName)) {
                disable(it)
                disableAny = true
            }
        }
        return disableAny
    }

    override fun disableAll(): Boolean {
        ArrayList(discoverables).forEach {
            disable(it)
        }
        return discoverables.size != 0
    }

    private fun disable(info: DiscoverableInfo) {
        // Live by the sword, die by the sword.
        // Misbehaving discoverables get disabled and won't come back until uninstall/reinstall.

        // If a plugin is detected in the stack of a crash then this will be called for that
        // plugin, if the plugin causing a crash cannot be identified, they are all disabled
        // assuming one of them must be bad.
        Log.w(TAG, "Disabling plugin ${info.component}")
        pm.setComponentEnabledSetting(
                info.component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }

    override fun dependsOn(discoverableInfo: DiscoverableInfo, cls: KClass<*>): Boolean {
        return versionMap[discoverableInfo]?.hasClass(cls) ?: false
        /*
        val plugins = ArrayList<DiscoverableInfo>(discoverables)
        return plugins.find { it.component.packageName == p::class.qualifiedName }?.version?.hasClass(cls) ?: false
        */
    }

    override fun toString(): String {
        return String.format("%s@%s (action=%s)", this::class.simpleName, hashCode(), action)
    }

    fun findFlags(cls: KClass<*>): Map<KClass<*>, Int> {
        control.staticProviders.forEach { list ->
            list.flags[cls]?.let {
                return it
            }
        }
        return emptyMap()
    }

    inner class PluginHandler: CoroutineScope {
        override val coroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

        suspend fun queryAll() {
            if (control.debugEnabled) Log.d(TAG, "queryAll $action")
            discoverables.forEach {
                listener!!.onRemoved(it)
                //if (!(it.plugin is PluginFragment)) {
                    // Only call onDestroy for discoverables that aren't fragments, as fragments
                    // will get the onDestroy as part of the fragment lifecycle.
                    //it.plugin.onDestroy()
                    //TODO("Do this in onPluginRemoved")
                //}
                //manager.discoverableInfoMap.remove(it.plugin)
            }
            discoverables.clear()
            handleQueryPlugins()
        }

        fun removePackage(pkg: String) {
            launch {
                discoverables.forEach {
                    if (it.component.packageName == pkg) {
                        handlePluginRemoved(it)
                        discoverables.remove(it)
                    }
                }
            }
        }

        fun queryPackage(pkg: String) {
            launch {
                if (control.debugEnabled) Log.d(TAG, "queryPkg $pkg $action")
                if (allowMultiple || (discoverables.size == 0)) {
                    handleQueryPlugins(pkg)
                } else {
                    if (control.debugEnabled) Log.d(TAG, "Too many of $action")
                }
            }
        }

        private suspend fun handlePluginDiscovered(info: I) {
            discoverablePrefs.setHasPlugins()
            withContext(Dispatchers.Main) {
                manager.handleWtfs()
                //manager.discoverableInfoMap[info.plugin] = info

                //if (!(msg.obj is PluginFragment)) {
                    // Only call onCreate for discoverables that aren't fragments, as fragments
                    // will get the onCreate as part of the fragment lifecycle.
                    //info.plugin.onCreate()
                    //TODO("Do this in onPluginDiscovered()")
                //}
                listener!!.onDiscovered(info)
            }
        }

        fun handlePluginRemoved(info: I) {
            versionMap.remove(info)
            launch(Dispatchers.Main) {
                if (control.debugEnabled) Log.d(TAG, "onPluginDisconnected")
                listener!!.onRemoved(info)
                //if (!(msg.obj is PluginFragment)) {
                    // Only call onDestroy for discoverables that aren't fragments, as fragments
                    // will get the onDestroy as part of the fragment lifecycle.
                    //info.plugin.onDestroy()
                    TODO("Do this in onPluginRemoved()")
                //}
                //manager.discoverableInfoMap.remove(info.plugin)
            }
        }

        private suspend fun handleQueryPlugins(pkgName: String? = null, notify: Boolean = true) {
            // This isn't actually a service and shouldn't ever be started, but is
            // a convenient PM based way to manage our discoverables.
            val intent = Intent(action)
            if (pkgName != null) {
                intent.setPackage(pkgName)
            }
            val result: MutableList<ResolveInfo> = pm.queryIntentServices(intent, 0)
            if (control.debugEnabled) Log.d(TAG, "Found ${result.size} discoverables for $action")
            if (result.size > 1 && !allowMultiple) {
                // TODO: Show warning.
                Log.w(TAG, "Multiple discoverables found for $action")
                return
            }

            listener!!.onStartDiscovering()

            withContext(Dispatchers.Default) {
                result.forEach {
                    launch(coroutineContext) {
                        val name = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
                        handleDiscoverPlugin(name)?.let { info ->
                            if (notify) {
                                handlePluginDiscovered(info)
                            }
                            discoverables.add(info)
                        }
                    }
                }
            }

            listener.onDoneDiscovering()
        }

        private fun handleDiscoverPlugin(component: ComponentName): I? {
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

                return try {
                    if (control.debugEnabled) Log.d(TAG, "discoverPlugin: $cls")

                    // Create our own ClassLoader so we can use our own code as the parent.
                    val classLoader = manager.getClassLoader(info.sourceDir, info.packageName)

                    @Suppress("UNCHECKED_CAST")
                    val dClass = (factoryManager.findClass(cls)
                        ?: classLoader.loadClass(cls).kotlin.also { factoryManager.addClass(cls, it) } )
                        as KClass<out Discoverable>

                    val dVersion = checkVersion(dClass, version)

                    val discoverableContext =
                        DiscoverableContextWrapper(
                            context,
                            context.createPackageContext(pkg, 0),
                            classLoader,
                            pkg
                        )

                    val serviceInfo = pm.getServiceInfo(component, PackageManager.GET_META_DATA)

                    discoverableInfoFactory.create(
                        this@DiscoverableManagerImpl,
                        discoverableContext, dClass, component, serviceInfo
                    ).also {
                        versionMap.put(it, dVersion)
                    }
                } catch (e: InvalidVersionException) {

                    notifyInvalidVersion(component, cls, e.tooNew, e.message)

                    Log.w(TAG, "Plugin $cls version check failed: ${e.message}")
                    null
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
        private fun checkVersion(cls: KClass<*>, version: VersionInfo): VersionInfo {
            val pv: VersionInfo = VersionInfo(
                control,
                factoryManager
            ).addClass(cls)
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
