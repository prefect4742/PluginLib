package com.prefect47.pluginlib.discoverables.factory

import com.prefect47.pluginlib.Discoverable
import kotlin.reflect.KClass

/**
 * Keeps track of Factory discoverables, and holds mapping between discoverable
 * class names and their corresponding KClass<*> instances.
 */
interface FactoryManager {
    val factories: List<FactoryDiscoverable>

    fun track(action: String)
    suspend fun start()

    fun addClass(className: String, cls: KClass<*>)
    fun findClass(className: String): KClass<*>?
    fun findRequirements(cls: KClass<*>): List<FactoryDiscoverable.Require>?

    fun <T: Discoverable> createInstance(cls: KClass<T>): T?
    //TODO: Methods for looking up everything should be placed here, for use by PluginInfo, DiscoverableManager and VersionInfo
}
