package com.prefect47.pluginlib.plugin

interface PluginPreferenceDataStoreProvider {
    fun getPreferenceDataStore(plugin: Plugin): PluginPreferenceDataStore
}
