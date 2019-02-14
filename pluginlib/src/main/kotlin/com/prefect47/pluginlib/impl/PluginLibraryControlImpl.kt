package com.prefect47.pluginlib.impl

import android.util.ArrayMap
import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*
import kotlin.reflect.KClass

object PluginLibraryControlImpl: PluginLibraryControl {
    private val listeners = ArrayList<PluginLibraryControl.StateListener>()
    private val trackers = HashMap<KClass<*>, PluginTracker>()
    private val sharedPreferencesHandlers: ArrayMap<String, PluginSharedPreferencesHandler> = ArrayMap()

    override var settingsHandler: PluginListCategory.SettingsHandler? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override var currentSharedPreferencesHandler: PluginSharedPreferencesHandler? = null

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
                tracker.start()
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

    override fun getMetaDataList(cls: KClass<*>): List<PluginMetadata>? {
        return trackers[cls]?.pluginList?.map { it.metadata }
    }

    override fun getMetaDataList(pluginClassName: String): List<PluginMetadata>? {
        return getMetaDataList(Class.forName(pluginClassName).kotlin)
    }

    override fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>? {
        return Dependency[PluginManager::class].pluginClassFlagsMap.get(pluginClassName)
    }

    override fun getMetaData(className: String): PluginMetadata? {
        for ((_, tracker) in trackers) {
            tracker.pluginList.find { it.metadata.className == className }?.let { return it.metadata }
        }
        return null
    }

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
}
