package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import com.prefect47.pluginlib.plugin.PluginSettings
import kotlinx.android.synthetic.main.plugin_pref.view.*
import kotlinx.android.synthetic.main.plugin_setting.view.*

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 * This preference is meant to be part of a list where more than one can be selected, since it inherits
 * CheckBoxPreference.
 */
class PluginMultiListEntry(
    context: Context, private val overrideKey: String, layoutResId: Int, private val plugin: Plugin
): CheckBoxPreference(context) {
    private val prefs: PluginPreferenceDataStore by lazy { preferenceDataStore as PluginPreferenceDataStore }

    init {
        layoutResource = layoutResId
        title = plugin.title
        summary = plugin.description
        icon = plugin.icon
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
                it.visibility = View.INVISIBLE
                it.setOnClickListener(null)
            }
        }
    }

    override fun persistBoolean(value: Boolean): Boolean {
        val current = prefs.getStringSet(overrideKey, mutableSetOf())!!
        if (value) {
            current.add(plugin.className)
        } else {
            current.remove(plugin.className)
        }
        prefs.putStringSet(overrideKey, current)
        return true
    }

    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean =
        prefs.getStringSet(overrideKey, emptySet())!!.contains(plugin.className)
}
