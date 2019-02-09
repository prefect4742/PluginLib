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
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import java.util.EnumSet
import kotlin.reflect.KClass

interface PluginLibraryControl {
    companion object {
        val DEFAULT_PERMISSIONNAME = "com.prefect47.pluginlib.permission.PLUGIN"
        val DEFAULT_DEBUGTAG = "PluginLib"
    }

    var currentSharedPreferencesHandler: PluginSharedPreferencesHandler?
    var settingsHandler: PluginListCategory.SettingsHandler?
    var permissionName: String
    var debugEnabled: Boolean
    var debugTag: String
    var notificationChannel: String?
    var notificationIconResId: Int

    fun addTracker(tracker: PluginTracker)

    /**
     * Add a [filter] to the plugin classloader that lets plugins use libraries or code where class names might
     * conflict with those of the app.
     */
    fun addClassFilter(filter: (String) -> Boolean)

    fun getMetaDataList(cls: KClass<*>): List<PluginMetadata>?
    fun getMetaDataList(pluginClassName: String): List<PluginMetadata>?

    fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>?

    fun getMetaData(className: String): PluginMetadata?

    fun addSharedPreferencesHandler(key: String, handler: PluginSharedPreferencesHandler)
    fun removeSharedPreferencesHandler(key: String)
    fun switchSharedPreferencesHandler(key: String)

    fun debug(msg: String) {
        if (debugEnabled) Log.d(debugTag, msg)
    }
}
