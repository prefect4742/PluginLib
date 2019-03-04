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

package com.prefect47.pluginlib.plugin

import android.util.Log
import com.prefect47.pluginlib.PluginLibDependencies
import com.prefect47.pluginlib.impl.Manager
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import com.prefect47.pluginlib.viewmodel.PluginListViewModel
import java.util.EnumSet
import kotlin.reflect.KClass

interface PluginLibraryControl {
    companion object {
        const val DEFAULT_PERMISSIONNAME = "com.prefect47.pluginlib.permission.PLUGIN"
        const val DEFAULT_DEBUGTAG = "PluginLib"
    }

    interface StateListener {
        fun onStarted() // All added trackers have loaded their plugins
        fun onPaused()
        fun onResumed()
        fun onStopped()
    }

    var staticPluginDependencies: PluginLibDependencies
    var settingsHandler: PluginListCategory.SettingsHandler?
    var permissionName: String
    var debugEnabled: Boolean
    var debugTag: String
    var notificationChannel: String?
    var notificationIconResId: Int

    val preferenceDataStoreManager: PluginPreferenceDataStoreManager

    val viewModel: PluginListViewModel

    suspend fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>,
        action: String = Manager.getAction(cls), allowMultiple : Boolean = false)
    fun removePluginListener(listener: PluginListener<*>)

    fun track(cls: KClass<out Plugin>)

    fun addStateListener(listener: StateListener)
    fun removeStateListener(listener: StateListener)

    suspend fun start()
    fun pause()
    fun resume()
    fun stop()

    suspend fun startPlugin(plugin: Plugin)
    suspend fun stopPlugin(plugin: Plugin)

    /**
     * Add a [filter] to the plugin classloader that lets plugins use libraries or code where class names might
     * conflict with those of the app.
     */
    fun addClassFilter(filter: (String) -> Boolean)

    fun getPluginList(cls: KClass<out Plugin>): List<Plugin>?
    fun getPluginList(pluginClassName: String): List<Plugin>?

    fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>?

    fun getPlugin(className: String): Plugin?

    fun debug(msg: String) {
        if (debugEnabled) Log.d(debugTag, msg)
    }
}
