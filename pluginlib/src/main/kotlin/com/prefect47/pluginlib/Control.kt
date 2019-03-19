/*
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

package com.prefect47.pluginlib

import android.util.Log
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager
import com.prefect47.pluginlib.discoverables.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginSettingsEntrance
import com.prefect47.pluginlib.discoverables.factory.FactoryManager

interface Control {
    companion object {
        const val DEFAULT_PERMISSIONNAME = "com.prefect47.pluginlib.permission.PLUGIN"
        const val DEFAULT_DEBUGTAG = "PluginLib"
    }

    interface StateListener {
        fun onStarted() // All added trackers have loaded their discoverables
        fun onPaused()
        fun onResumed()
        fun onStopped()
    }

    val staticProviders: List<Providers>

    var settingsHandler: PluginSettingsEntrance.Callback?

    var permissionName: String
    var debugEnabled: Boolean
    var debugTag: String
    var notificationChannel: String?
    var notificationIconResId: Int

    val manager: Manager
    val factoryManager: FactoryManager
    val pluginManager: PluginManager
    val preferenceDataStoreManager: PluginPreferenceDataStoreManager

    fun addStaticProviders(providers: Providers)

    fun addStateListener(listener: StateListener)
    fun removeStateListener(listener: StateListener)

    suspend fun start()
    fun pause()
    fun resume()
    fun stop()

    //suspend fun startPlugin(plugin: Plugin)
    //suspend fun stopPlugin(plugin: Plugin)

    /**
     * Add a [filter] to the discoverable classloaders that lets discoverables use libraries or code where class names
     * might conflict with those of the app or this library.
     * Returning true from a filter that a discoverable is allowed to load that class from the app. This includes using
     * reflection, so be careful.
     */
    fun addClassFilter(filter: (String) -> Boolean)

    fun debug(msg: String) {
        if (debugEnabled) Log.d(debugTag, msg)
    }
}
