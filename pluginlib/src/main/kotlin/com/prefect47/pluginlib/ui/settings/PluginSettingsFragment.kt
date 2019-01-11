package com.prefect47.pluginlib.ui.settings

import android.os.Bundle
import android.view.*
import com.prefect47.pluginlib.plugin.PluginSettings
import com.prefect47.pluginlib.ui.PluginFragment

class PluginSettingsFragment : PluginFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val layoutResId = (metadata.plugin as PluginSettings).layoutResId
        return inflater.inflate(layoutResId, container, false)
    }
}
