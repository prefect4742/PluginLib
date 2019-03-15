package com.prefect47.pluginlibimpl.discoverables.factory

/**
 * Keeps track of Factory discoverables.
 */
interface FactoryManager {
    fun track(action: String)
    suspend fun start()

    //TODO: Methods for looking up everything should be placed here, for use by PluginInfo, DiscoverableManager and VersionInfo
}
