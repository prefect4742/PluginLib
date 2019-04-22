package com.prefect47.pluginlib.ui.settings

import android.os.Bundle
import android.view.*
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.discoverables.plugin.PluginSettings
import com.prefect47.pluginlib.ui.PluginFragment

open class PluginSettingsFragment : PluginFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val layoutResId = pluginInfo.getInt(PluginSettings.FRAGMENTLAYOUT, R.layout.plugin_settings_fragment)
        return inflater.inflate(layoutResId!!, container, false)
    }
}
