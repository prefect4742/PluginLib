package com.prefect47.pluginlib.impl

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager as Manager
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreProvider

object PluginDefaultPreferenceDataStoreProvider: PluginPreferenceDataStoreProvider {
    private val served = mutableMapOf<Plugin, PluginPreferenceDataStore>()

    private class DataStore(val plugin: Plugin): PluginPreferenceDataStore() {
        private val listener = OnSharedPreferenceChangeListener { _, key -> notifyPreferenceChanged(key) }
        private val prefs = plugin.pluginContext.getSharedPreferences("preferences", Context.MODE_PRIVATE).also {
            it.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun getBoolean(key: String?, defValue: Boolean) = prefs.getBoolean(key, defValue)
        override fun getFloat(key: String?, defValue: Float) = prefs.getFloat(key, defValue)
        override fun getInt(key: String?, defValue: Int) = prefs.getInt(key, defValue)
        override fun getLong(key: String?, defValue: Long) = prefs.getLong(key, defValue)
        override fun getString(key: String?, defValue: String?): String?  = prefs.getString(key, defValue)
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
                prefs.getStringSet(key, defValues)

        override fun putBoolean(key: String?, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
        override fun putFloat(key: String?, value: Float) = prefs.edit().putFloat(key, value).apply()
        override fun putInt(key: String?, value: Int) = prefs.edit().putInt(key, value).apply()
        override fun putLong(key: String?, value: Long) = prefs.edit().putLong(key, value).apply()
        override fun putString(key: String?, value: String?) = prefs.edit().putString(key, value).apply()
        override fun putStringSet(key: String?, values: MutableSet<String>?) =
                prefs.edit().putStringSet(key, values).apply()
    }

    override fun getPreferenceDataStore(plugin: Plugin): PluginPreferenceDataStore {
        TODO("Map to package, not plugin - plugins in the same package shall be able to read each others data")
        served[plugin]?.let { return it }
        val newStore = DataStore(plugin)
        served.put(plugin, newStore)
        return newStore
    }
}
