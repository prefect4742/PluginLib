package com.prefect47.pluginlib.plugin

import android.content.SharedPreferences

interface PluginSharedPreferencesHandler {
    fun getSharedPreferences(name: String?, mode: Int): SharedPreferences?
}
