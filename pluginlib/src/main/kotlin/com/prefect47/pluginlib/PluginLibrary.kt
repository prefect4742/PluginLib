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

import android.content.Context
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.impl.PluginManager
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.plugin.PluginTracker
import com.prefect47.pluginlib.plugin.PluginTrackerList
import com.prefect47.pluginlib.ui.PluginPreference
import kotlin.reflect.KClass

object PluginLibrary {
    lateinit var settingsHandler: PluginPreference.SettingsHandler
    val trackers = HashMap<KClass<*>, PluginTrackerList<*>>()

    /**
     * Initialize the plugin library for an app plugin permission [permissionName] and whose plugins packages start with
     * [clientPluginClassPrefix]. While developing the app, [debugPlugins] should be true so that crashes do not cause
     * plugin components to be disabled.
     */
    fun init(context: Context, permissionName: String, clientPluginClassPrefix: String,
             debugPlugins: Boolean, debugTag: String) {
        Dependency.init(context)
        Dependency[PluginManager::class].setPermissionName(permissionName)
        Dependency[PluginManager::class].setClientPluginClassPrefix(clientPluginClassPrefix)
        Dependency[PluginManager::class].setDebugPlugins(debugPlugins, debugTag)
        Dependency[PluginManager::class].debug("PluginLib initialized")
    }

    inline fun <reified T : Plugin> track(cls: KClass<T>) {
        trackers[cls] = Dependency[PluginTracker::class].create(cls)
        Dependency[PluginManager::class].debug("Tracking $cls")
    }

    inline fun <reified T : Plugin> getPlugins(cls: KClass<T>): List<T>? {
        val tracker: PluginTrackerList<T>? = trackers[cls] as PluginTrackerList<T>
        return tracker?.map { it.plugin }
    }

    fun getMetaDataList(pluginClassName: String): List<PluginMetadata>? {
        val pluginClass = Class.forName(pluginClassName).kotlin
        return trackers[pluginClass]?.map { it.metadata }
    }

    fun getMetaData(className: String): PluginMetadata? {
        for ((_, list) in trackers) {
            list.find { it.metadata.className == className }?.let { return it.metadata }
        }
        return null
    }

    fun setPluginSettingsHandler(handler: PluginPreference.SettingsHandler) {
        settingsHandler = handler
    }
}
