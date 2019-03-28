package com.prefect47.pluginlib.discoverables.factory

import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.annotations.ProvidesInterface
import kotlin.reflect.KClass

@ProvidesInterface(version = FactoryDiscoverable.VERSION)
interface FactoryDiscoverable: Discoverable {
    companion object {
        const val VERSION = 1
    }

    data class Require(val target: KClass<*>, val version: Int)

    val classMap: Map<String, KClass<*>>
    fun <T: Discoverable> createInstance(cls: KClass<out Discoverable>): T?

    val requirements: Map<KClass<*>, List<Require>>
}
