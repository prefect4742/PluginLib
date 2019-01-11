package com.prefect47.pluginlib.plugin

import android.content.SharedPreferences
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface

@ProvidesInterface(version = PluginSettings.VERSION)
interface PluginSettings: SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val VERSION = 1
    }

    val layoutResId: Int
        get() = R.layout.plugin_settings_fragment

    val preferencesResId: Int

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}
}
