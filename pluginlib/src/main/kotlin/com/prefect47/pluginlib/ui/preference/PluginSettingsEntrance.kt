package com.prefect47.pluginlib.ui.preference

import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo

interface PluginSettingsEntrance {
    interface Callback {
        fun openSettings(pluginInfo: PluginInfo<out Plugin>, prefsKey: String?)
    }

    val pluginInfo: PluginInfo<out Plugin>
}
