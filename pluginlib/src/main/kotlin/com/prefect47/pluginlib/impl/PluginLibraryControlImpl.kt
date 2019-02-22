package com.prefect47.pluginlib.impl

import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginLibraryControlImpl @Inject constructor(private val managerLazy: Lazy<PluginManager>,
        override val preferenceDataStoreManager: PluginPreferenceDataStoreManager): PluginLibraryControl {
    private val listeners = ArrayList<PluginLibraryControl.StateListener>()
    private val trackers = HashMap<KClass<*>, PluginTracker>()

    // We need to get() this double-lazily since we can't call it in the constructor since that would introduce a
    // circular call loop and exhaust the stack.
    private val manager: PluginManager by lazy { managerLazy.get() }

    override var settingsHandler: PluginListCategory.SettingsHandler? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override fun addClassFilter(filter: (String) -> Boolean) {
        manager.addClassFilter(filter)
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

    override fun getPluginList(pluginClassName: String): List<Plugin>? =
        getPluginList(Class.forName(pluginClassName).kotlin)

    override fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>? =
        manager.pluginClassFlagsMap[pluginClassName]

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
