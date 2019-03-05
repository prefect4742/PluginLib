package com.prefect47.pluginlib.plugin

import kotlin.reflect.KClass

interface PluginLibProviders {
    data class Provider(val action: String, val version: Int)

    val providers: Map<KClass<*>, Provider>
    val dependencies: Map<KClass<*>, List<KClass<*>>>
}
