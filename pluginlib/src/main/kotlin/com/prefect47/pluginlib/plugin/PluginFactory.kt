package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import kotlin.reflect.KClass

@ProvidesInterface(version = PluginFactory.VERSION)
interface PluginFactory: PluginLifecycle {
    companion object {
        const val VERSION = 1
    }

    interface Listener: ConnectionListener<PluginFactory>

    fun <T: Plugin> createInstance(cls: KClass<out Plugin>): T?
}
