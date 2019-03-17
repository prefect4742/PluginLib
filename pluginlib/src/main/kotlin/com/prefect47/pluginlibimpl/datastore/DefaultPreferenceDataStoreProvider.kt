package com.prefect47.pluginlibimpl.datastore

import android.content.Context
import android.content.SharedPreferences
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager as Manager
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreProvider
import java.lang.IllegalArgumentException

class DefaultPreferenceDataStoreProvider(private val context: Context):
    PluginPreferenceDataStoreProvider {

    private open class DataStore(context: Context, name: String): PluginPreferenceDataStore() {
        protected val prefs: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

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

    private val defaultStore: PluginPreferenceDataStore by lazy {
        DataStore(
            context,
            "preferences"
        )
    }
    private val cache = mutableMapOf<String, PluginPreferenceDataStore>()

    private class PluginDataStore(context: Context, name: String): DataStore(context, name) {
        init {
            prefs.registerOnSharedPreferenceChangeListener { _, key -> notifyPreferenceChanged(key) }
        }
    }

    override fun getPreferenceDataStore() = defaultStore

    override fun getPreferenceDataStore(key: Any): PluginPreferenceDataStore {
        if (key is PluginInfo<out Plugin>) {
            val listKey = key.data.getString(PluginInfo.LIST_KEY)?.let { "_$it" } ?: ""
            val name = "${key.component.packageName}${listKey}_settings"
            cache[name]?.let { return it }
            val newStore = PluginDataStore(context, name)
            cache[name] = newStore
            return newStore
        } else {
            throw IllegalArgumentException("Unrecognized session key $key")
        }
    }
}
