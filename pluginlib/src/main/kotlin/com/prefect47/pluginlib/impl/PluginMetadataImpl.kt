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
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginMetadata

class PluginMetadataImpl(override val plugin: Plugin, val context: Context, override val pluginContext: Context,
                         override val pkg: String,
                         override val className: String, val classLoader: ClassLoader) : PluginMetadata {
    private val defaultIcon: Drawable by lazy { ContextCompat.getDrawable(context, R.drawable.ic_no_icon)!! }

    companion object Factory: PluginMetadataFactory {
        override fun create(
            plugin: Plugin, context: Context, pluginContext: Context, pkg: String, cls: String, classLoader: ClassLoader
        ): PluginMetadata {
            return PluginMetadataImpl(plugin, context, pluginContext, pkg, cls, classLoader)
        }
    }

    private val data: Bundle by lazy {
        val myService = ComponentName(pluginContext, className)
        pluginContext.packageManager.getServiceInfo(myService, PackageManager.GET_META_DATA).metaData
    }

    private fun getString(key: String): String {
        data.getInt("${key}Res").let { if (it !=0) return pluginContext.resources.getString(it) }
        data.getString(key)?.let {return it}
        return "NO_STRING"
    }

    private fun getDrawable(): Drawable {
        data.getInt("pluginIconRes").let {
            if (it != 0 ) ContextCompat.getDrawable(pluginContext, it)?.let { drawable -> return drawable }
        }
        return defaultIcon
    }

    override var enabled: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(pluginContext).getBoolean(className, false)
        set(value) {}

    override var title: String
        get() = getString("pluginTitle")
        set(value) {}

    override var description: String
        get() = getString("pluginDescription")
        set(value) {}

    override var icon: Drawable
        get() = getDrawable()
        set(value) {}
}
