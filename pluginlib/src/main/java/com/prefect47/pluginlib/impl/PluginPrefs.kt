/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 Niklas Brunlid
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.prefect47.pluginlib.impl

import android.content.Context
import android.content.SharedPreferences

/**
 * Storage for all plugin actions in SharedPreferences.
 *
 * This allows the list of actions to search for to be generated instead of hard coded.
 */
class PluginPrefs(val context: Context) {
    companion object {
        private const val PREFS = "plugin_prefs"
        private const val PLUGIN_ACTIONS = "actions"
        private const val HAS_PLUGINS = "com/prefect47/pluginlib/plugin"
    }

    private val pluginActions: MutableSet<String>
    private val sharedPrefs: SharedPreferences

    val pluginList: Set<String>
        get() = pluginActions

    init {
        sharedPrefs = context.getSharedPreferences(PREFS, 0)
        val prefs: Set<String>? = sharedPrefs.getStringSet(PLUGIN_ACTIONS, null)
        pluginActions = prefs?.let { HashSet(prefs) } ?: HashSet()
    }

    @Synchronized
    fun addAction(action: String) {
        if (pluginActions.add(action)) {
            sharedPrefs.edit().putStringSet(PLUGIN_ACTIONS, pluginActions).commit()
        }
    }

    fun hasPlugins(): Boolean {
        return sharedPrefs.getBoolean(HAS_PLUGINS, false)
    }

    fun setHasPlugins() {
        sharedPrefs.edit().putBoolean(HAS_PLUGINS, true).commit()
    }
}
