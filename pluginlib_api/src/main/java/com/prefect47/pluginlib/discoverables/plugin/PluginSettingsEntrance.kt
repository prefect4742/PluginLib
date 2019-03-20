package com.prefect47.pluginlib.discoverables.plugin

interface PluginSettingsEntrance {
    interface Callback {
        fun openSettings(pluginInfo: PluginInfo<out Plugin>)
    }
}
