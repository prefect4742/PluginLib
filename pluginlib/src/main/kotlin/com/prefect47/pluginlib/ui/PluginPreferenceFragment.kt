package com.prefect47.pluginlib.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.plugin.PluginMetadata

/**
 * Settings fragment that inflates a preference XML resource owned by the plugin.
 */
class PluginPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var metadata: PluginMetadata

    @SuppressLint("RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val className = arguments!!.getString("className")
        metadata = PluginLibrary.getMetaData(className!!)!!

        val xmlRoot = preferenceManager.inflateFromResource(
            metadata.pluginContext, metadata.getSettingsResId(), null) as PreferenceScreen

        val root: Preference
        if (rootKey != null) {
            root = xmlRoot.findPreference(rootKey)
            if (root !is PreferenceScreen) {
                throw IllegalStateException("Preference object with key $rootKey is not a referenceScreen")
            }
        } else {
            root = xmlRoot
        }

        preferenceScreen = root
    }
}
