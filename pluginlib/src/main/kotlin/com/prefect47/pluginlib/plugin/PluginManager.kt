package com.prefect47.pluginlib.plugin

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

    val list: Map<KClass<out Plugin>, PluginList<out Plugin>>

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

    /**
     * Get flags from annotations on the [pluginClass] interface.
     */
    fun <T: Plugin> getFlags(pluginClass: KClass<T>): Set<String>
    fun getFlags(pluginClassName: String): Set<String>
}
