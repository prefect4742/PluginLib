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
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import java.util.EnumSet
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties

object PluginLibrary {
    const val ARG_CLASSNAME = "pluginClassName"

    lateinit var settingsHandler: PluginListCategory.SettingsHandler
    val trackers = HashMap<KClass<*>, PluginTrackerList<*>>()

    /**
     * Initialize the plugin library for an app plugin permission [permissionName], sending notifications to
     * [notificationChannel] with icon [notificationIconResId].
     * While developing the app, [debugPlugins] should be true (and [debugTag] set) so that crashes do not cause plugin
     * components to be disabled.
     */
    fun init(context: Context, notificationChannel: String, notificationIconResId: Int, permissionName: String,
             debugPlugins: Boolean, debugTag: String) {
        Dependency.init(context)
        Dependency[PluginManager::class].setNotification(notificationChannel, notificationIconResId)
        Dependency[PluginManager::class].setPermissionName(permissionName)
        Dependency[PluginManager::class].setDebugPlugins(debugPlugins, debugTag)
        Dependency[PluginManager::class].debug("PluginLib initialized")
    }

    /**
     * Add a [filter] to the plugin classloader that lets plugins use libraries or code where class names might
     * conflict with those of the app.
     */
    fun addClassFilter(filter: (String) -> Boolean) {
        Dependency[PluginManager::class].addClassFilter(filter)
    }

    inline fun <reified T : Plugin> track(cls: KClass<T>) {
        trackers[cls] = Dependency[PluginTracker::class].create(cls)
        Dependency[PluginManager::class].debug("Tracking $cls")
    }

    inline fun <reified T : Plugin> getPlugins(cls: KClass<T>): List<T>? {
        val tracker: PluginTrackerList<T>? = trackers[cls] as PluginTrackerList<T>
        return tracker?.map { it.plugin }
    }

    fun getMetaDataList(cls: KClass<*>): List<PluginMetadata>? {
        return trackers[cls]?.map { it.metadata }
    }

    fun getMetaDataList(pluginClassName: String): List<PluginMetadata>? {
        return getMetaDataList(Class.forName(pluginClassName).kotlin)
    }

    fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>? {
        return Dependency[PluginManager::class].pluginClassFlagsMap.get(pluginClassName)
    }

    fun getMetaData(className: String): PluginMetadata? {
        for ((_, list) in trackers) {
            list.find { it.metadata.className == className }?.let { return it.metadata }
        }
        return null
    }

    fun setPluginSettingsHandler(handler: PluginListCategory.SettingsHandler) {
        settingsHandler = handler
    }
}
