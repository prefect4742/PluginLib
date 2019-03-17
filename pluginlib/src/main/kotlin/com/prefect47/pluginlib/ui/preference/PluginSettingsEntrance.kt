package com.prefect47.pluginlib.ui.preference

import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo

interface PluginSettingsEntrance {
    interface Callback {
        fun openSettings(pluginInfo: PluginInfo<out Plugin>)
    }
}
