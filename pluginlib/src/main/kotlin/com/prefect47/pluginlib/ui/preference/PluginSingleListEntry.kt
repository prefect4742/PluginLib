package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.plugin.PluginSettings
import kotlinx.android.synthetic.main.plugin_pref.view.*
import kotlinx.android.synthetic.main.plugin_setting.view.*

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 * This preference is meant to be part of a list where only one can be selected, since it inherits
 * SwitchPreference.
 */
class PluginSingleListEntry(context: Context, layoutResId: Int,
                            private val metadata: PluginMetadata) : SwitchPreference(context) {
    init {
        layoutResource = layoutResId
        title = metadata.plugin.title
        summary = metadata.plugin.description
        icon = metadata.plugin.icon
        key = metadata.plugin::class.qualifiedName
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.settings_frame?.let {
            if (metadata.plugin is PluginSettings) {
                it.visibility = View.VISIBLE
                it.settings_button?.setOnClickListener {
                    Dependency[PluginLibraryControl::class].settingsHandler?.openSettings(metadata.plugin)
                }
            } else {
                it.visibility = View.GONE
                it.setOnClickListener(null)
            }
        }
    }
}
