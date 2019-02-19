package com.prefect47.pluginlib.plugin

import androidx.preference.PreferenceDataStore

abstract class PluginPreferenceDataStore: PreferenceDataStore() {
    private val listeners = mutableListOf<OnPluginPreferenceDataStoreChangeListener>()

    interface OnPluginPreferenceDataStoreChangeListener {
        fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String)
    }

    infix fun registerOnPluginPreferenceDataStoreChangeListener(listener: OnPluginPreferenceDataStoreChangeListener) {
        listeners.add(listener)
    }

    infix fun unregisterOnPluginPreferenceDataStoreChangeListener(listener: OnPluginPreferenceDataStoreChangeListener) {
        listeners.remove(listener)
    }

    protected fun notifyPreferenceChanged(key: String) {
        listeners.forEach {
            it.onDataStorePreferenceChanged(this, key)
        }
    }
}
