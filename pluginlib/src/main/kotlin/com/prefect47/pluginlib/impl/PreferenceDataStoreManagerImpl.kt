package com.prefect47.pluginlib.impl

import android.content.Context
import com.prefect47.pluginlib.plugin.*
import javax.inject.Inject

class PreferenceDataStoreManagerImpl @Inject constructor(context: Context): PluginPreferenceDataStoreManager {

    override var provider: PluginPreferenceDataStoreProvider = DefaultPreferenceDataStoreProvider(context)

    // Remember which instances have asked so that they can be invalidated id needed.
    //private val served = mutableSetOf<PluginInfo<out Plugin>>()

    // Cache the data stores returned by the provider on a per-package basis. Plugins in the same package should get
    // the same data store so that they can share data.
    //private val cache = mutableMapOf<String, PluginPreferenceDataStore>()

    override fun getPreferenceDataStore(): PluginPreferenceDataStore = provider.getPreferenceDataStore()

    override fun getPreferenceDataStore(pluginInfo: PluginInfo<out Plugin>): PluginPreferenceDataStore =
        provider.getPreferenceDataStore(pluginInfo)
        /*{
        served.add(pluginInfo)
        cache[pluginInfo.component.packageName]?.let { return it }
        val newStore = provider.getPreferenceDataStore(pluginInfo)
        cache[pluginInfo.component.packageName] = newStore
        return newStore
    }*/

    /**
     * Call this when the provider has new PreferenceDataStore instances to serve and the instances should no longer
     * use the ones they have.
     */
    /*
    override fun invalidate() {
        val oldServed = served.toSet()
        served.clear()
        cache.clear()
        //oldServed.forEach { it.onPreferenceDataStoreInvalidated() }
    }
    */
}
