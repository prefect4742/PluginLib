package com.prefect47.pluginlib.impl.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R

/**
 * Inserts a settings icon into a Preference if if contains a settings_frame element.
 */
@SuppressLint("RestrictedApi")
class PluginPreferenceGroupAdapter(preferenceGroup: PreferenceGroup) : PreferenceGroupAdapter(preferenceGroup) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)

        val settingsWidgetFrame = holder.findViewById(R.id.settings_frame) as? ViewGroup
        if (settingsWidgetFrame != null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.plugin_setting, settingsWidgetFrame)
        }

        return holder
    }
}
