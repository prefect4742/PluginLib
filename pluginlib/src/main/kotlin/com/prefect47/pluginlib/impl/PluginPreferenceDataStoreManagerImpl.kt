package com.prefect47.pluginlib.impl

import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager as Manager
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreProvider as Provider

object PluginPreferenceDataStoreManagerImpl: Manager {

    override lateinit var provider: Provider

    private val served = mutableSetOf<Plugin>()

    internal fun init(defaultProvider: Provider) {
        provider = defaultProvider
    }

    override fun getPreferenceDataStore(plugin: Plugin): PluginPreferenceDataStore {
        served.add(plugin)
        return provider.getPreferenceDataStore(plugin)
    }

    /**
     * Call this when the provider has new PreferenceDataStore instances to serve and the plugins should no longer
     * use the ones they have.
     */
    override fun invalidate() {
        served.forEach { it.onPreferenceDataStoreInvalidated() }
        served.clear()
    }
}
