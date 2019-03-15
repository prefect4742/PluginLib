package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface

@ProvidesInterface(version = PluginSettings.VERSION)
interface PluginSettings: PluginPreferenceDataStore.OnPluginPreferenceDataStoreChangeListener {

    companion object {
        const val VERSION = 1
        const val FRAGMENTLAYOUT = "settingsLayout" // meta-data: layout used to hold the xml prefs
        const val PREFERENCES = "preferences"       // meta-data: preferences for the plugin
    }

    interface Layout {
        val fragmentLayout: Int
    }

    // Called when app invalidates the data store(s). This can happen if the app uses sessions and
    // wishes all its discoverables to switch to another set of preferences.
    // Plugin should reload whatever preferences it has cached.
    override fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String) {}
}
