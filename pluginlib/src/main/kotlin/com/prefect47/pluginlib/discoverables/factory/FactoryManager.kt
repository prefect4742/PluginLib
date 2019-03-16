package com.prefect47.pluginlib.discoverables.factory

import kotlin.reflect.KClass

/**
 * Keeps track of Factory discoverables.
 */
interface FactoryManager {
    val factories: List<FactoryDiscoverable>

    fun track(action: String)
    suspend fun start()

    fun findClass(cls: String): KClass<*>
    fun findRequirements(cls: KClass<*>): List<FactoryDiscoverable.Require>?
    //TODO: Methods for looking up everything should be placed here, for use by PluginInfo, DiscoverableManager and VersionInfo
}
