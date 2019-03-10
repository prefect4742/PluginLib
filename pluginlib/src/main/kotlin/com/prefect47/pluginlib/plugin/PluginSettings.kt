package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface

@ProvidesInterface(version = PluginSettings.VERSION)
interface PluginSettings: PluginPreferenceDataStore.OnPluginPreferenceDataStoreChangeListener {

    companion object {
        const val VERSION = 1
    }

    val layoutResId: Int
        get() = R.layout.plugin_settings_fragment

    val preferencesResId: Int

    override fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String) {}

    // Called when app changes the PluginSharedPreferencesFactory. This can happen if the app uses sessions and
    // wishes all its instances to switch to another set of preferences.
    // Plugin should reload whatever preferences it has cached.
    //fun onSharedPreferenceHandlerChanged() {}
}
