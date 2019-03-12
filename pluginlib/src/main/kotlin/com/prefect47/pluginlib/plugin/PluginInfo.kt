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


package com.prefect47.pluginlib.plugin

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle

interface PluginInfo<T: Plugin>: PluginPreferenceDataStore.OnPluginPreferenceDataStoreChangeListener {
    companion object {
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val ICON = "icon"
    }

    val pluginContext: Context
    val data: Bundle
    val component: ComponentName

    fun start(): T
    fun stop()

    /** Check if the meta-data for the plugin type declares contains the given key **/
    fun containsKey(key: String): Boolean

    /** Get the integer value that the meta-data for the plugin type declares, if any **/
    fun getInt(key: String, default: Int): Int?

    /** Get the resource string that the meta-data for the plugin type declares, if any **/
    fun getStringResource(key: String, default: String? = null): String?

    /** Get the resource drawable that the meta-data for the plugin type declares, if any **/
    fun getDrawableResource(key: String, default: Drawable? = null): Drawable?
}
