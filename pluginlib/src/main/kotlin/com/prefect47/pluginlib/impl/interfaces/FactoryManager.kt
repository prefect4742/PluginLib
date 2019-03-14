package com.prefect47.pluginlib.impl.interfaces

/**
 * Keeps track of PluginFactory instances.
 */
interface FactoryManager {
    //val list: Map<KClass<out Plugin>, PluginListModel<out Plugin>>

    fun track(action: String)
    suspend fun start()

    //TODO: Methods for looking up everything should be placed here, for use by PluginInfo, InstanceManager and VersionInfo
}
