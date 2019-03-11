package com.prefect47.pluginlib.plugin

interface PluginPreferenceDataStoreProvider {
    /**
     * Get the current data store.
     */
    fun getPreferenceDataStore(): PluginPreferenceDataStore

    /**
     * Get the current data store for the given plugin. Note that instances in the same package must get the same
     * data store.
     */
    fun getPreferenceDataStore(pluginInfo: PluginInfo<out Plugin>): PluginPreferenceDataStore
}
