package com.prefect47.pluginlib

import kotlin.reflect.KClass

interface PluginLibDependencies {
    data class Provider(val action: String, val version: Int)

    val providers: Map<KClass<*>, Provider>
    val dependencies: Map<KClass<*>, List<KClass<*>>>
}
