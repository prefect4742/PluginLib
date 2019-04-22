package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
import com.prefect47.pluginlib.discoverables.plugin.PluginSettings

/**
 * Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 * This preference is meant to be part of a list where only one can be selected, since it inherits
 * SwitchPreference.
 */
class PluginSingleListEntry(
    context: Context, private val overrideKey: String, layoutResId: Int, val pluginInfo: PluginInfo<out Plugin>
): SwitchPreferenceCompat(context) {
    private val prefs: PluginPreferenceDataStore by lazy { preferenceDataStore as PluginPreferenceDataStore }

    init {
        layoutResource = layoutResId
        widgetLayoutResource = R.layout.plugin_radiobutton
        title = pluginInfo.getStringResource(PluginInfo.TITLE)
        summary = pluginInfo.getStringResource(PluginInfo.DESCRIPTION)
        icon = pluginInfo.getDrawableResource(PluginInfo.ICON) ?: context.getDrawable(R.drawable.ic_no_icon)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.findViewById<LinearLayout>(R.id.settings_frame)?.let {
            if (pluginInfo.containsKey(PluginSettings.PREFERENCES)) {
                it.visibility = View.VISIBLE
                it.findViewById<Button>(R.id.settings_button)?.setOnClickListener {
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
