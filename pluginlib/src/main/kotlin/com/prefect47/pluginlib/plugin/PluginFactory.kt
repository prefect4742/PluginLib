package com.prefect47.pluginlib.plugin

import kotlin.reflect.KClass

interface PluginFactory {
    fun <T: Plugin> createInstance(cls: KClass<out Plugin>): T?
}
