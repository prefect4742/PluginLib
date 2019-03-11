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

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.prefect47.pluginlib.impl.interfaces.InstanceInfo
import com.prefect47.pluginlib.impl.interfaces.PluginInfoFactory
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import javax.inject.Inject

class PluginInfoImpl<T: Plugin> (
    instanceInfo: InstanceInfo<T>
): PluginInfo<T> {

    class Factory @Inject constructor(
    ): PluginInfoFactory {
        override fun <T : Plugin> create(instanceInfo: InstanceInfo<T>): PluginInfo<T> =
            PluginInfoImpl(instanceInfo)
    }

    companion object {
        private const val TAG = "PluginInfo"
    }

    override val pluginContext = instanceInfo.pluginContext
    override val component = instanceInfo.component
    override val metadata = instanceInfo.metadata

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
