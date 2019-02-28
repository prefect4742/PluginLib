package com.prefect47.pluginlib.ui.preference

import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.impl.ui.PluginPreferenceGroupAdapter

/**
 * Settings fragment that allows PluginPreferenceGroupAdapter to enable/disable library-specific elements in the
 * preference layout.
 */
abstract class PluginListPreferenceFragment : PreferenceFragmentCompat() {

    // We override this so that we can set the PreferenceDataStore as early as possible.
    override fun setPreferencesFromResource(preferencesResId: Int, key: String?) {
        preferenceManager.preferenceDataStore = PluginLibraryDI.component.getDataStoreManager().getPreferenceDataStore()
        super.setPreferencesFromResource(preferencesResId, key)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return PluginPreferenceGroupAdapter(preferenceScreen)
    }
}
