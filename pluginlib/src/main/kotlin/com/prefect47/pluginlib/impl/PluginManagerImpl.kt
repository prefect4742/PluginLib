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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.net.Uri
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import android.util.ArrayMap
import android.util.ArraySet
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.prefect47.pluginlib.impl.PluginInstanceManager.PluginInfo
import com.prefect47.pluginlib.plugin.*
import dalvik.system.PathClassLoader
import kotlinx.coroutines.*
import java.lang.Thread.UncaughtExceptionHandler
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties

/**
 * @see Plugin
 */
class PluginManagerImpl(val context: Context,
                        defaultHandler: UncaughtExceptionHandler
        ) : BroadcastReceiver(), PluginManager {

    companion object: PluginManager.Factory {
        override fun create(
            context: Context,
            defaultHandler: Thread.UncaughtExceptionHandler
        ): PluginManager {
            return PluginManagerImpl(PluginAppContextWrapper(context), defaultHandler)
        }
    }

    @Inject lateinit var pluginPrefs: PluginPrefs

    override val pluginInfoMap: MutableMap<Plugin, PluginInfo<*>> = HashMap()
    override val pluginClassFlagsMap: MutableMap<String, EnumSet<Plugin.Flag>> = HashMap()

    private val pluginMap: MutableMap<PluginListener<*>, PluginInstanceManager<out Plugin>> =
            ArrayMap<PluginListener<*>, PluginInstanceManager<*>>()
    private val classLoaders: MutableMap<String, ClassLoader> = ArrayMap<String, ClassLoader>()
    private val oneShotPackages: MutableSet<String> = ArraySet<String>()

    // Lazily load this so it doesn't have any effect on devices without plugins.
    private val parentClassLoader: ClassLoaderFilterInternal by lazy {
        val filter = ClassLoaderFilterInternal(this::class.java.classLoader!!)
        filter.filters.add { name ->
            name.startsWith("com.prefect47.pluginlib") &&
                    !name.startsWith("com.prefect47.pluginlib.plugin") &&
                    !name.startsWith("com.prefect47.pluginlib.ui")
        }
        filter
    }
    private var isListening: Boolean = false
    private var hasOneShot: Boolean = false
    private var isWtfsSet : Boolean = false

    private val notificationId = PluginManager.nextNotificationId

    private val factory: PluginInstanceManager.Factory by lazy { Dependency[PluginInstanceManager.Factory::class] }

    init {
        Dependency.component.inject(this)

        val uncaughtExceptionHandler = PluginExceptionHandler(defaultHandler)
        //Thread.setUncaughtExceptionPreHandler(uncaughtExceptionHandler)
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler) // TODO: What is the difference here?

        // Needed
        //Dependency.get(PluginDependencyProvider::class)

        /*
        Handler(looper).post(() -> {
            // Plugin dependencies that don't have another good home can go here, but
            // dependencies that have better places to init can happen elsewhere.
            Dependency.get(PluginDependencyProvider.class)
                    .allowPluginDependency(ActivityStarter.class)
        }
        */

        /*
        val r = context.resources
        val channel = NotificationChannel(PluginManager.NOTIFICATION_CHANNEL_ID,
            r.getString(R.string.plugin_channel_name), NotificationManager.IMPORTANCE_HIGH )
        channel.metadata = r.getString(R.string.plugin_channel_description)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        */
    }

    override fun <T: Plugin> getOneShotPlugin(cls: KClass<T>, action: String): T? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("Must be called from UI thread")
        }
        val p: PluginInstanceManager<T> = factory.create(action, null, false, cls)
        //val p: PluginInstanceManager<T> = factory.create(context, action, null, false, cls, this)
        pluginPrefs.addAction(action)
        //Dependency[PluginPrefs::class].addAction(action)
        val info: PluginInfo<T>? = p.getPlugin()
        if (info != null) {
            oneShotPackages.add(info.pkg)
            hasOneShot = true
            startListening()
            return info.plugin
        }
        return null
    }

    override suspend fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>, action: String,
                                               allowMultiple: Boolean) {
        pluginPrefs.addAction(action)
        //Dependency[PluginPrefs::class].addAction(action)
        val p: PluginInstanceManager<T> = factory.create(action, listener, allowMultiple, cls)
        //val p: PluginInstanceManager<T> = factory.create(context, action, listener, allowMultiple, cls, this)
        p.loadAll()
        pluginMap[listener] = p
        startListening()

        var flags = EnumSet.noneOf(Plugin.Flag::class.java)
        val result = cls.companionObject?.declaredMemberProperties?.find { it.name == "FLAGS" }
        if (result is KProperty1) {
            result as KProperty1<Any?, EnumSet<Plugin.Flag>>
            flags = result.get(cls.companionObjectInstance)
        }
        pluginClassFlagsMap[cls.qualifiedName!!] = flags
    }

    override fun removePluginListener(listener: PluginListener<*>) {
        if (!pluginMap.containsKey(listener)) return
        pluginMap.remove(listener)?.destroy()
        if (pluginMap.isEmpty()) {
            stopListening()
        }
    }

    private fun startListening() {
        if (isListening) return
        isListening = true
        var filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(PluginManager.PLUGIN_CHANGED)
        filter.addAction(PluginManager.DISABLE_PLUGIN)
        filter.addDataScheme("package")
        context.registerReceiver(this, filter)
        filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
        context.registerReceiver(this, filter)
    }

    private fun stopListening() {
        // Never stop listening if a one-shot is present.
        if (!isListening || hasOneShot) return
        isListening = false
        context.unregisterReceiver(this)
    }

    @Override
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Intent.ACTION_USER_UNLOCKED -> {
                GlobalScope.launch(Dispatchers.Default) {
                    pluginMap.values.forEach { it.loadAll() }
                }
            }
            PluginManager.DISABLE_PLUGIN -> {
                val uri: Uri = intent.data!!
                val component = ComponentName.unflattenFromString(uri.toString().substring(10))!!
                context.packageManager.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                context.getSystemService(NotificationManager::class.java).cancel(notificationId)
            }
            else -> {
                val data: Uri = intent.data!!
                val pkg: String = data.encodedSchemeSpecificPart
                if (oneShotPackages.contains(pkg)) {
                    val color = Resources.getSystem().getIdentifier(
                        "system_notification_accent_color", "color", "android"
                    )
                    var label: String = pkg
                    try {
                        val pm: PackageManager = context.packageManager
                        label = pm.getApplicationInfo(pkg, 0).loadLabel(pm).toString()
                    } catch (e: NameNotFoundException) {
                    }

                    Dependency[PluginLibraryControl::class].let { control ->
                        control.notificationChannel?.let { channel ->
                            val nb = NotificationCompat.Builder(context, channel)
                                .setWhen(0)
                                .setShowWhen(false)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setColor(context.getColor(color))
                                .setContentTitle("Plugin \"$label\" has updated")
                                .setContentText("Restart ExtScanner for changes to take effect.")

                            control.notificationIconResId.let {
                                if (it != 0) nb.setSmallIcon(it)
                            }

                            val i: Intent = Intent("com.sony.extendablemediascanner.action.RESTART").setData(
                                Uri.parse("package://$pkg")
                            )
                            val pi: PendingIntent = PendingIntent.getBroadcast(context, 0, i, 0)
                            nb.addAction(Action.Builder(0, "Restart ExtScanner", pi).build())
                            NotificationManagerCompat.from(context).notify(notificationId, nb.build())
                        }
                    }
                }
                if (clearClassLoader(pkg)) {
                    Toast.makeText(context, "Reloading $pkg", Toast.LENGTH_LONG).show()
                }
                if (Intent.ACTION_PACKAGE_REMOVED != intent.action) {
                    pluginMap.values.forEach { it.onPackageChange(pkg) }
                } else {
                    pluginMap.values.forEach { it.onPackageRemoved(pkg) }
                }
            }
        }
    }

    override fun getClassLoader(sourceDir: String, pkg: String): ClassLoader {
        if (classLoaders.containsKey(pkg)) {
            return classLoaders[pkg]!!
        }
        val classLoader: ClassLoader = PathClassLoader(sourceDir, parentClassLoader)
        classLoaders[pkg] = classLoader
        return classLoader
    }

    private fun clearClassLoader(pkg: String): Boolean {
        return classLoaders.remove(pkg) != null
    }

    @Throws(NameNotFoundException::class)
    fun getContext(info: ApplicationInfo, pkg: String): Context {
        val classLoader: ClassLoader = getClassLoader(info.sourceDir, pkg)
        return PluginContextWrapper(context, context.createPackageContext(pkg, 0), classLoader, pkg)
    }

    override fun <T: Any> dependsOn(p: Plugin, cls: KClass<T>): Boolean {
        pluginMap.forEach {
            if (it.value.dependsOn(p, cls)) return true
        }
        return false
    }

    override fun handleWtfs() {
        if (!isWtfsSet) {
            isWtfsSet = true
            /*
            Log.setWtfHandler((tag, what, system) -> {
                throw CrashWhilePluginActiveException(what)
            })
            */
        }
    }

    override fun getApplicationContext(): Context = context

    override fun addClassFilter(filter: (String) -> Boolean) {
        parentClassLoader.filters.add(filter)
    }

        // This allows plugins to include any libraries or copied code they want by only including
    // classes from the plugin library.
    private class ClassLoaderFilterInternal(val base: ClassLoader) : ClassLoader(getSystemClassLoader()) {
        val filters = ArrayList<(String) -> Boolean>()

        @Throws(ClassNotFoundException::class)
        override fun loadClass(name: String, resolve: Boolean): Class<*>? {

            for (filter in filters) {
                if (filter.invoke(name)) {
                    super.loadClass(name, resolve)
                    break
                }
            }
            return base.loadClass(name)
        }
    }

    inner class PluginExceptionHandler(private val handler: UncaughtExceptionHandler): UncaughtExceptionHandler {

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            var theThrowable: Throwable = throwable

            if (Dependency[PluginLibraryControl::class].debugEnabled) {
                handler.uncaughtException(thread, throwable)
                return
            }

            // Search for and disable plugins that may have been involved in this crash.
            var disabledAny: Boolean = checkStack(throwable)
            if (!disabledAny) {
                // We couldn't find any plugins involved in this crash, just to be safe
                // disable all the plugins, so we can be sure that the app is running as
                // best as possible.
                pluginMap.values.forEach { disabledAny = disabledAny || it.disableAll() }
            }

            if (disabledAny) {
                theThrowable = CrashWhilePluginActiveException(throwable)
            }

            // Run the normal exception handler so we can crash and cleanup our state.
            handler.uncaughtException(thread, theThrowable)
        }

        private fun checkStack(throwable: Throwable?): Boolean {
            if (throwable == null) return false
            var disabledAny = false
            throwable.stackTrace.forEach { element ->
                pluginMap.values.forEach {
                    disabledAny = disabledAny || it.checkAndDisable(element.className)
                }
            }
            return disabledAny || checkStack(throwable.cause)
        }
    }

    private class CrashWhilePluginActiveException(throwable: Throwable): RuntimeException(throwable)
}
