package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import kotlin.reflect.KClass

@ProvidesInterface(version = PluginFactory.VERSION)
interface PluginFactory {
    companion object {
        const val VERSION = 1
    }

    fun <T: Plugin> createInstance(cls: KClass<out Plugin>): T?
}
