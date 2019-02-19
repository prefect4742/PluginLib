package com.prefect47.pluginlib.plugin

interface PluginPreferenceDataStoreManager: PluginPreferenceDataStoreProvider {
    /**
     * If the app wants to it can implement its own provider. One example is it it uses sessions and when switching
     * to another, all settings shall be read/written elsewhere.
     * If app doesn't set one, a default will be used that stores to default SharedPreferences and prefixes all
     * keys with the plugins package name(s).
     */
    var provider: PluginPreferenceDataStoreProvider

    /**
     * Call this when the provider has new PreferenceDataStore instances to serve and the plugins should no longer
     * use the ones they have.
     */
    fun invalidate()
}