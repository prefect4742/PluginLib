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

package com.prefect47.pluginlib.impl

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginMetadata

class PluginMetadataImpl(override val plugin: Plugin,
                         override val pluginContext: Context,
                         override val pkg: String,
                         override val className: String) : PluginMetadata {
    companion object Factory: PluginMetadataFactory {
        override fun create(plugin: Plugin, pluginContext: Context, pkg: String, cls: String
        ): PluginMetadata {
            return PluginMetadataImpl(plugin, pluginContext, pkg, cls)
        }
    }

    private val data: Bundle by lazy {
        val myService = ComponentName(pluginContext, className)
        pluginContext.packageManager.getServiceInfo(myService, PackageManager.GET_META_DATA).metaData
    }

    override val enabled: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(pluginContext).getBoolean(className, false)
}
