package com.prefect47.pluginlib.impl

import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import java.util.*
import kotlin.reflect.KClass

object PluginLibraryControlImpl: PluginLibraryControl {
    internal val trackers = HashMap<KClass<*>, PluginTracker>()

    override var settingsHandler: PluginListCategory.SettingsHandler? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override fun addTracker(tracker: PluginTracker) {
        trackers[tracker.pluginClass] = tracker
        debug("Tracking ${tracker.pluginClass}")
    }

    override fun addClassFilter(filter: (String) -> Boolean) {
        Dependency[PluginManager::class].addClassFilter(filter)
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

    }

    override fun removeSharedPreferencesHandler(key: String) {

    }

    override fun switchSharedPreferencesHandler(key: String) {

    }
}
