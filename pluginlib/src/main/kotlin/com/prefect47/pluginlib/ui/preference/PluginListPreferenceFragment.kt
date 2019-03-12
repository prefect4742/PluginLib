package com.prefect47.pluginlib.ui.preference

import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.prefect47.pluginlib.impl.extensions.preferencesRecursive
import com.prefect47.pluginlib.impl.ui.PluginPreferenceGroupAdapter

/**
 * Settings fragment that allows PluginPreferenceGroupAdapter to enable/disable library-specific elements in the
 * preference layout.
 */
abstract class PluginListPreferenceFragment : PreferenceFragmentCompat() {
    val settingsEntrances: List<PluginSettingsEntrance>
        get() = preferenceScreen.preferencesRecursive.filterIsInstance<PluginSettingsEntrance>()

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return PluginPreferenceGroupAdapter(preferenceScreen)
    }
}
