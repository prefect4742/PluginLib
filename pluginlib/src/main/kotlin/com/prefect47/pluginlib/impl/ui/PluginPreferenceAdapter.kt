package com.prefect47.pluginlib.impl.ui

import android.annotation.SuppressLint
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.ui.preference.PluginPreference
import com.prefect47.pluginlib.ui.preference.PluginPreferencesFragment

/**
 * Provides added functionality to PluginPreferences that need access to e.g. the fragment.
 */
@SuppressLint("RestrictedApi")
class PluginPreferenceAdapter(preferenceGroup: PreferenceGroup, val fragment: PluginPreferencesFragment)
        : PreferenceGroupAdapter(preferenceGroup) {
    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val preference = getItem(position)
        when (preference) {
            is PluginPreference -> preference.parentFragment = fragment
        }
    }
}
