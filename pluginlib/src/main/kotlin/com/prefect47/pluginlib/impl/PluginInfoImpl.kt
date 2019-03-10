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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.prefect47.pluginlib.impl.interfaces.InstanceManager.InstanceInfo
import com.prefect47.pluginlib.impl.interfaces.PluginInfoFactory
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import javax.inject.Inject

class PluginInfoImpl<T: Plugin> (
    override val metadata: Bundle, override val component: ComponentName, override val pluginContext: Context
): PluginInfo<T> {

    class Factory @Inject constructor(
        context: Context
    ): PluginInfoFactory {

        private val pm = context.packageManager

        override fun <T : Plugin> create(component: ComponentName, pluginContext: Context): PluginInfo<T> {
            val serviceInfo = pm.getServiceInfo(component, PackageManager.GET_META_DATA)
            val metadata = serviceInfo.metaData ?: Bundle()
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            metadata.putInt(PluginInfo.DESCRIPTION, serviceInfo.descriptionRes)
            metadata.putInt(PluginInfo.ICON, serviceInfo.icon)
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            return PluginInfoImpl(metadata, component, pluginContext)
        }
    }

    companion object {
        private const val TAG = "PluginInfo"
    }

    override fun getString(key: String): String? {
        return try {
            pluginContext.getString(metadata.getInt(key))
        } catch (e: Resources.NotFoundException) {
            null
        }
    }
    override fun getDrawable(key: String): Drawable? {
        return try {
            pluginContext.getDrawable(metadata.getInt(key))
        } catch (e: Resources.NotFoundException) {
            null
        }
    }

    override fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String) {
        // TODO: Call plugin instance on the same interface if it is currently running
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
