package com.prefect47.pluginlib.impl

import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginMetadata

object PluginMetadataMap {
    private val map: MutableMap<Plugin, PluginMetadata> = HashMap()

    fun add(metadata: PluginMetadata) {
        map.put(metadata.plugin, metadata)
    }

    fun remove(plugin: Plugin) {
        map.remove(plugin)
    }

    fun get(plugin: Plugin): PluginMetadata? {
        return map.get(plugin)
    }
}
