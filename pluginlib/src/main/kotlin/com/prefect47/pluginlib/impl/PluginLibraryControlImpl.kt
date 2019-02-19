package com.prefect47.pluginlib.impl

import android.util.ArrayMap
import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KClass

object PluginLibraryControlImpl: PluginLibraryControl {
    private val listeners = ArrayList<PluginLibraryControl.StateListener>()
    private val trackers = HashMap<KClass<*>, PluginTracker>()

    override var settingsHandler: PluginListCategory.SettingsHandler? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override val preferenceDataStoreManager
        get() = Dependency[PluginPreferenceDataStoreManager::class]

    override fun addClassFilter(filter: (String) -> Boolean) {
        Dependency[PluginManager::class].addClassFilter(filter)
    }

    override fun addTracker(tracker: PluginTracker) {
        trackers[tracker.pluginClass] = tracker
        debug("PluginLib tracking ${tracker.pluginClass.qualifiedName}")
    }

    override fun addStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.remove(listener)
    }

    override suspend fun start() {
        debug("PluginLib starting")
        GlobalScope.async(Dispatchers.Default) {
            for ((_, tracker) in trackers) {
                launch { tracker.start() }
            }
        }.join()
        debug("PluginLib started")
        listeners.forEach { it.onStarted() }
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPluginList(cls: KClass<*>): List<Plugin>? = trackers[cls]?.pluginList

    override fun getPluginList(pluginClassName: String): List<Plugin>? {
        return getPluginList(Class.forName(pluginClassName).kotlin)
    }

    override fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>? {
        return Dependency[PluginManager::class].pluginClassFlagsMap[pluginClassName]
    }

    override fun getPlugin(className: String): Plugin? {
        for ((_, tracker) in trackers) {
            tracker.pluginList.find { it::class.qualifiedName == className }?.let { return it }
        }
        return null
    }

    /*
    override fun addSharedPreferencesHandler(key: String, handler: PluginSharedPreferencesHandler) {
        sharedPreferencesHandlers[key] = handler
    }

    override fun removeSharedPreferencesHandler(key: String) {
        sharedPreferencesHandlers.remove(key)
    }

    override fun switchSharedPreferencesHandler(key: String) {
        currentSharedPreferencesHandler = sharedPreferencesHandlers[key] ?:
                throw IllegalArgumentException("PluginSharedPreferencesHandler with key $key not found")
        for ((_, tracker) in trackers) {
            tracker.pluginList.filterIsInstance<PluginSettings>().forEach { it.onSharedPreferenceHandlerChanged() }
        }
    }
    */
}
