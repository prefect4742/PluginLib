package com.prefect47.pluginlib.plugin

interface PluginPreferenceDataStoreProvider {
    /**
     * Get the current data store.
     */
    fun getPreferenceDataStore(): PluginPreferenceDataStore

    /**
     * Get the current data store for the given plugin. Note that plugins in the same package will get the same
     * data store.
     */
    fun getPreferenceDataStore(plugin: Plugin): PluginPreferenceDataStore
}
