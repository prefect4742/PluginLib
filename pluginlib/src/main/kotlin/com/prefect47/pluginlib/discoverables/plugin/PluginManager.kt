package com.prefect47.pluginlib.discoverables.plugin

import com.prefect47.pluginlib.DiscoverableManager
import kotlin.reflect.KClass

/**
 * Keeps track of Plugin discoverables.
 */
interface PluginManager {
    interface PluginList<T: Plugin> {
        val plugins: List<PluginDiscoverableInfo>
        val discoverableManager: DiscoverableManager<T, PluginDiscoverableInfo>
    }

    interface PluginInfoHook {
        fun onPluginInfoCreated(pluginInfo: PluginInfo<out Plugin>)
    }

    val pluginInfoMap: MutableMap<Plugin, PluginInfo<out Plugin>>

    val list: Map<KClass<out Plugin>, PluginList<out Plugin>>

    /**
     * You can add hooks to PluginInfo creation that will be called at certain points of the PluginInfo
     * lifecycle.
     */
    val hooks: MutableList<PluginInfoHook>

    fun track(cls: KClass<out Plugin>)
    suspend fun start()

    /**
     * Get a list of PluginInfo containers for any plugin implementing the [pluginClass] interface.
     * Note that each call to this will return new PluginInfo instances.
     */
    fun <T: Plugin> getList(pluginClass: KClass<T>): List<PluginInfo<T>>?
    fun getList(pluginClassName: String): List<PluginInfo<out Plugin>>?

    /**
     * Get a PluginInfo container for the plugin [cls].
     * Note that each call to this will return a new instance of the container.
     */
    operator fun <T: Plugin> get(cls: KClass<T>): PluginInfo<T>?
    operator fun get(clsName: String): PluginInfo<out Plugin>?
}
