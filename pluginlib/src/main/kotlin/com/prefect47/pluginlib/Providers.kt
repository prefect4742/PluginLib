package com.prefect47.pluginlib

import kotlin.reflect.KClass

interface Providers {
    data class Provider(val action: String, val version: Int)

    val classMap: Map<String, KClass<*>>
    val providers: Map<KClass<*>, Provider>
    val dependencies: Map<KClass<*>, List<KClass<*>>>
    val flags: Map<KClass<*>, Map<KClass<*>, Int>>
}
