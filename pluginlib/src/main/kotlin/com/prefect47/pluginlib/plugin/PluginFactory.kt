package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import kotlin.reflect.KClass

@ProvidesInterface(version = PluginFactory.VERSION)
interface PluginFactory: Discoverable {
    companion object {
        const val VERSION = 1
    }

    data class Require(val target: KClass<*>, val version: Int)

    val implementations: Map<String, KClass<*>>
    fun <T: Plugin> createInstance(cls: KClass<out Plugin>): T?

    val requirements: Map<KClass<*>, List<Require>>
}
