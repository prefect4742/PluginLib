package com.prefect47.pluginlib.viewmodel

import com.prefect47.pluginlib.plugin.Plugin
import kotlin.reflect.KClass

/**
 * Viewmodel that will maintain lists of available plugins of the class it's told to trask.
 * The lists will be populated automatically and can be used in databindings.
 */
interface PluginListViewModel {
    val list: Map<KClass<out Plugin>, PluginListModel<out Plugin>>

    fun track(cls: KClass<out Plugin>)
}
