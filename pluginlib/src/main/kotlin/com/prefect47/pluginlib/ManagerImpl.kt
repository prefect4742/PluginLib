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

package com.prefect47.pluginlib

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import android.util.ArraySet
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.prefect47.pluginlib.DiscoverableInfo.Listener
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import dagger.Lazy
import dalvik.system.PathClassLoader
import kotlinx.coroutines.*
import java.lang.Thread.UncaughtExceptionHandler
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * @see Plugin
 */
class ManagerImpl(
    private val context: Context, private val control: Control, private val factoryManagerLazy: Lazy<FactoryManager>,
    private val discoverablePrefs: DiscoverablePrefs, private val factoryLazy: Lazy<DiscoverableManager.Factory>,
    defaultHandler: UncaughtExceptionHandler
) : BroadcastReceiver(), Manager {

    class Factory @Inject constructor(
        private val context: Context, private val control: Control, private val factoryManagerLazy: Lazy<FactoryManager>,
        private val discoverablePrefs: DiscoverablePrefs, private val factoryLazy: Lazy<DiscoverableManager.Factory>
    ): Manager.Factory {
        override fun create(defaultHandler: Thread.UncaughtExceptionHandler) = ManagerImpl(
            AppContextWrapper(context),
            control,
            factoryManagerLazy,
            discoverablePrefs,
            factoryLazy,
            defaultHandler
        )
    }

    private val discoverableManagerMap: MutableMap<Listener<*>, DiscoverableManager<out Discoverable, out DiscoverableInfo>> =
            Collections.synchronizedMap(HashMap())
    private val classLoaders: MutableMap<String, ClassLoader> = Collections.synchronizedMap(HashMap())
    private val oneShotPackages: MutableSet<String> = Collections.synchronizedSet(ArraySet())

    // Lazily load this so it doesn't have any effect on devices without discoverables.
    private val parentClassLoader: ClassLoaderFilterInternal by lazy {
        val filter = ClassLoaderFilterInternal(this::class.java.classLoader!!)

        /* Returning true from a filter that a discoverable is allowed to load that class from the library. This
         * includes using reflection and so we have to be careful.
         * Since we use the classloaders of the discoverables ourselves to look up annotations, those have to be
         * allowed. */
        filter.filters.add { name ->
            name.startsWith("com.prefect47.pluginlib")
                    && !name.startsWith("com.prefect47.pluginlib.annotations")
        }
        filter
    }
    private var isListening: Boolean = false
    private var hasOneShot: Boolean = false
    private var isWtfsSet : Boolean = false

    private val notificationId = Manager.nextNotificationId

    // We need to get() this double-lazily since we can't call it in the constructor since that would introduce a
    // circular call loop and exhaust the stack.
    private val factory: DiscoverableManager.Factory by lazy { factoryLazy.get() }
    private val factoryManager: FactoryManager by lazy { factoryManagerLazy.get() }

    init {
        val uncaughtExceptionHandler = PluginExceptionHandler(defaultHandler)
        //Thread.setUncaughtExceptionPreHandler(uncaughtExceptionHandler)
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler) // TODO: What is the difference here?

        /*
        Handler(looper).post(() -> {
            // Plugin dependencies that don't have another good home can go here, but
            // dependencies that have better places to init can happen elsewhere.
            Dependency.get(DependencyProviderImpl.class)
                    .allowPluginDependency(ActivityStarter.class)
        }
        */

        /*
        val r = context.resources
        val channel = NotificationChannel(Manager.NOTIFICATION_CHANNEL_ID,
            r.getString(R.string.plugin_channel_name), NotificationManager.IMPORTANCE_HIGH )
        channel.metadata = r.getString(R.string.plugin_channel_description)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        */
    }

    /*
    override fun <T: Plugin> getOneShotPlugin(cls: KClass<T>, action: String): T? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("Must be called from UI thread")
        }
        val p: DiscoverableManager<T> = factory.create(action, null, false, cls)
        discoverablePrefs.addAction(action)
        val info: DiscoverableInfo<T>? = p.getPlugin()
        if (info != null) {
            oneShotPackages.add(info.pkg)
            hasOneShot = true
            startListening()
            return info.plugin
        }
        return null
    }
    */
    private fun getAction(cls: KClass<*>): String {
        control.staticProviders.forEach {
            it.providers[cls]?.let { provider ->
                return provider.action
            }
        }

        /*
        cls.findAnnotation<ProvidesInterface>()?.let { info ->
            if (TextUtils.isEmpty(info.action)) {
                throw RuntimeException(cls.simpleName + " doesn't provide an action")
            }
            return info.action
        }
        */

        throw RuntimeException("${cls.simpleName} doesn't provide an interface")
    }

    override suspend fun <T : Discoverable, I : DiscoverableInfo> addListener(
        listener: Listener<I>,
        cls: KClass<T>,
        allowMultiple: Boolean,
        discoverableInfoFactory: DiscoverableInfo.Factory<I>
    ): DiscoverableManager<T, I> =
        addListener(listener, cls, getAction(cls), allowMultiple, discoverableInfoFactory)

    override suspend fun <T: Discoverable, I: DiscoverableInfo> addListener(
        listener: Listener<I>,
        cls: KClass<T>,
        action: String,
        allowMultiple: Boolean,
        discoverableInfoFactory: DiscoverableInfo.Factory<I>
    ): DiscoverableManager<T, I> {
        discoverablePrefs.addAction(action)
        val p: DiscoverableManager<T, I> = factory.create(action, listener, allowMultiple, cls, discoverableInfoFactory)
        p.loadAll()
        discoverableManagerMap[listener] = p
        //classManagerMap[cls.qualifiedName!!] = p
        startListening()
        return p
    }

    override fun <I: DiscoverableInfo> removeListener(listener: Listener<I>) {
        if (!discoverableManagerMap.containsKey(listener)) return
        discoverableManagerMap.remove(listener)?.also {
            //classManagerMap.remove(it.cls.qualifiedName)
            it.destroy()
        }
        if (discoverableManagerMap.isEmpty()) {
            stopListening()
        }
    }

    private fun startListening() {
        if (isListening) return
        isListening = true
        var filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Manager.PLUGIN_CHANGED)
        filter.addAction(Manager.DISABLE_PLUGIN)
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
                    discoverableManagerMap.values.forEach { it.loadAll() }
                }
            }
            Manager.DISABLE_PLUGIN -> {
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
                if (clearClassLoader(pkg)) {
                    Toast.makeText(context, "Reloading $pkg", Toast.LENGTH_LONG).show()
                }
                if (Intent.ACTION_PACKAGE_REMOVED != intent.action) {
                    discoverableManagerMap.values.forEach { it.onPackageChange(pkg) }
                } else {
                    discoverableManagerMap.values.forEach { it.onPackageRemoved(pkg) }
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

    override fun dependsOn(discoverableInfo: DiscoverableInfo, cls: KClass<*>): Boolean {
        return discoverableInfo.manager.dependsOn(discoverableInfo, cls)
        /*
        discoverableManagerMap.forEach {
            if (it.value.dependsOn(discoverableInfo., cls)) return true
        }
        return false
        */
    }

    override fun <T: Discoverable> getFlags(cls: KClass<T>, flagClass: KClass<*>) =
        discoverableManagerMap.values.find { it.cls == cls }?.flags?.get(flagClass) ?: 0

    @Suppress("UNCHECKED_CAST")
    override fun getFlags(clsName: String, flagClass: KClass<*>) =
        getFlags(factoryManager.findClass(clsName) as KClass<out Discoverable>, flagClass)

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

    // This allows discoverables to include any libraries or copied code they want by only including
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

            if (control.debugEnabled) {
                handler.uncaughtException(thread, throwable)
                return
            }

            // Search for and disable discoverables that may have been involved in this crash.
            var disabledAny: Boolean = checkStack(throwable)
            if (!disabledAny) {
                // We couldn't find any discoverables involved in this crash, just to be safe
                // disable all the discoverables, so we can be sure that the app is running as
                // best as possible.
                discoverableManagerMap.values.forEach { disabledAny = disabledAny || it.disableAll() }
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
                discoverableManagerMap.values.forEach {
                    disabledAny = disabledAny || it.checkAndDisable(element.className)
                }
            }
            return disabledAny || checkStack(throwable.cause)
        }
    }

    private class CrashWhilePluginActiveException(throwable: Throwable): RuntimeException(throwable)
}
