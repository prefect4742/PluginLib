package com.prefect47.pluginlib.impl

import android.content.Context
import com.prefect47.pluginlib.plugin.*
import javax.inject.Inject

class PreferenceDataStoreManagerImpl @Inject constructor(context: Context): PluginPreferenceDataStoreManager {
    override var provider: PluginPreferenceDataStoreProvider = DefaultPreferenceDataStoreProvider(context)

    override fun getPreferenceDataStore(): PluginPreferenceDataStore = provider.getPreferenceDataStore()

    override fun getPreferenceDataStore(key: Any): PluginPreferenceDataStore =
        provider.getPreferenceDataStore(key)
}
