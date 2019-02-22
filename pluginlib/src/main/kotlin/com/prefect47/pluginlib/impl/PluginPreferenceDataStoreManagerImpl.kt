package com.prefect47.pluginlib.impl

import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import javax.inject.Inject
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager as Manager
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreProvider as Provider

class PluginPreferenceDataStoreManagerImpl @Inject constructor(): Manager {

    override var provider: com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreProvider =
        PluginDefaultPreferenceDataStoreProvider

    // Remember which plugins have asked so that they can be invalidated id needed.
    private val served = mutableSetOf<Plugin>()

    // Cache the data stores returned by the provider on a per-package basis. Plugins in the same package should get
    // the same data store so that they can share data.
    private val cache = mutableMapOf<String, PluginPreferenceDataStore>()

    override fun getPreferenceDataStore(plugin: Plugin): PluginPreferenceDataStore {
        served.add(plugin)
        cache[plugin.pkgName]?.let { return it }
        val newStore = provider.getPreferenceDataStore(plugin)
        cache[plugin.pkgName] = newStore
        return newStore
    }

    /**
     * Call this when the provider has new PreferenceDataStore instances to serve and the plugins should no longer
     * use the ones they have.
     */
    override fun invalidate() {
        val oldServed = served.toSet()
        served.clear()
        cache.clear()
        oldServed.forEach { it.onPreferenceDataStoreInvalidated() }
    }
}
