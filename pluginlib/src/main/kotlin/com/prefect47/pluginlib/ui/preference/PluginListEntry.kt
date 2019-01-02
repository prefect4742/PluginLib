package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.plugin.PluginSettings

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 */
class PluginListEntry(context: Context, layoutResId: Int,
                      private val metadata: PluginMetadata) : SwitchPreference(context) {
    interface SettingsHandler {
        fun openSettings(metadata: PluginMetadata)
    }

    init {
        layoutResource = layoutResId
        title = metadata.getTitle()
        summary = metadata.getDescription()
        icon = metadata.getIcon()
        key = metadata.className
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.findViewById(R.id.settings_frame)?.let {
            if (metadata.plugin is PluginSettings) {
                it.visibility = View.VISIBLE
                it.findViewById<View>(R.id.settings_button)?.setOnClickListener {
                    PluginLibrary.settingsHandler.openSettings(metadata)
                }
            } else {
                it.visibility = View.GONE
                it.setOnClickListener(null)
            }
        }
    }
}
