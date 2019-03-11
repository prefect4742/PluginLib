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
import android.os.Bundle
import com.prefect47.pluginlib.impl.interfaces.InstanceInfo
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo
import javax.inject.Inject

class InstanceInfoImpl<T: Plugin> (
    override val pluginContext: Context, override val component: ComponentName, override val metadata: Bundle,
    override val version: VersionInfo?
): InstanceInfo<T> {

    class Factory @Inject constructor(
        context: Context
    ): InstanceInfo.Factory {

        private val pm = context.packageManager

        override fun <T : Plugin> create(pluginContext: Context, component: ComponentName,
                version: VersionInfo?): InstanceInfo<T> {
            val serviceInfo = pm.getServiceInfo(component, PackageManager.GET_META_DATA)
            val metadata = serviceInfo.metaData ?: Bundle()
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            metadata.putInt(PluginInfo.DESCRIPTION, serviceInfo.descriptionRes)
            metadata.putInt(PluginInfo.ICON, serviceInfo.icon)
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            return InstanceInfoImpl(pluginContext, component, metadata, version)
        }
    }

    companion object {
        private const val TAG = "InstanceInfo"
    }
}
