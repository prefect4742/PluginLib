package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import com.prefect47.pluginlib.plugin.PluginSettings
import kotlinx.android.synthetic.main.plugin_pref.view.*
import kotlinx.android.synthetic.main.plugin_setting.view.*

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 * This preference is meant to be part of a list where only one can be selected, since it inherits
 * SwitchPreference.
 */
class PluginSingleListEntry(
    context: Context, private val overrideKey: String, layoutResId: Int, private val pluginInfo: PluginInfo<out Plugin>
): SwitchPreferenceCompat(context) {
    private val prefs: PluginPreferenceDataStore by lazy { preferenceDataStore as PluginPreferenceDataStore }

    init {
        layoutResource = layoutResId
        widgetLayoutResource = R.layout.plugin_radiobutton
        title = pluginInfo.getString(PluginInfo.TITLE)
        summary = pluginInfo.getString(PluginInfo.DESCRIPTION)
        icon = pluginInfo.getDrawable(PluginInfo.ICON) ?: context.getDrawable(R.drawable.ic_no_icon)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val tag = holder.itemView.tag
        holder.itemView.settings_frame?.let {
            if (pluginInfo.metadata.containsKey(PluginSettings.PREFERENCES)) {
                it.visibility = View.VISIBLE
                it.settings_button?.setOnClickListener {
                    PluginLibraryDI.component.getControl().settingsHandler?.openSettings(pluginInfo)
                }
            } else {
                it.visibility = View.INVISIBLE
                it.setOnClickListener(null)
            }
        }
    }

    override fun persistBoolean(value: Boolean): Boolean {
        if (value) prefs.putString(overrideKey, pluginInfo.component.className)
        return true
    }


    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean =
        pluginInfo.component.className == prefs.getString(overrideKey, null)
}
