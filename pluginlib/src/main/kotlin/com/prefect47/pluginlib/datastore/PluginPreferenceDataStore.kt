package com.prefect47.pluginlib.datastore

import androidx.preference.PreferenceDataStore

abstract class PluginPreferenceDataStore: PreferenceDataStore() {
    private val listeners = mutableListOf<OnPluginPreferenceDataStoreChangeListener>()

    interface OnPluginPreferenceDataStoreChangeListener {
        fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String)
    }

    infix fun addListener(listener: OnPluginPreferenceDataStoreChangeListener) {
        listeners.add(listener)
    }

    infix fun removeListener(listener: OnPluginPreferenceDataStoreChangeListener) {
        listeners.remove(listener)
    }

    protected fun notifyPreferenceChanged(key: String) {
        listeners.forEach {
            it.onDataStorePreferenceChanged(this, key)
        }
    }
}
