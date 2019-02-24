package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginSettings
import kotlinx.android.synthetic.main.plugin_pref.view.*
import kotlinx.android.synthetic.main.plugin_setting.view.*

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 * This preference is meant to be part of a list where only one can be selected, since it inherits
 * SwitchPreference.
 */
class PluginSingleListEntry(
    context: Context, layoutResId: Int, private val plugin: Plugin
): SwitchPreference(context) {
    init {
        layoutResource = layoutResId
        title = plugin.title
        summary = plugin.description
        icon = plugin.icon
        key = plugin::class.qualifiedName
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.settings_frame?.let {
            if (plugin is PluginSettings) {
                it.visibility = View.VISIBLE
                it.settings_button?.setOnClickListener {
                    PluginLibraryDI.component.getControl().settingsHandler?.openSettings(plugin)
                }
            } else {
                it.visibility = View.GONE
                it.setOnClickListener(null)
            }
        }
    }
}
