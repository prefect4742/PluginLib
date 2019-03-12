package com.prefect47.pluginlib.plugin

interface PluginPreferenceDataStoreProvider {
    /**
     * Get the default data store.
     */
    fun getPreferenceDataStore(): PluginPreferenceDataStore

    /**
     * Get the data store matching the given key.
     * The library will mainly call this with the key being a PluginInfo<out Plugin>. Note that
     * instances of plugins in the same package must get the same data store.
     */
    fun getPreferenceDataStore(key: Any): PluginPreferenceDataStore
}
