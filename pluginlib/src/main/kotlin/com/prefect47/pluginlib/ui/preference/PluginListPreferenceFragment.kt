package com.prefect47.pluginlib.ui.preference

import android.os.Bundle
import androidx.fragment.app.Fragment
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
    abstract val key: Any?

    /**
     * Call this from your subclass if you wish to use the generic data store.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
                PluginLibraryDI.component.getDataStoreManager().getPreferenceDataStore()
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return PluginPreferenceGroupAdapter(preferenceScreen, key)
    }
}
