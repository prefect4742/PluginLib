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
import android.os.Bundle
import android.util.Log
import com.prefect47.pluginlib.impl.instances.PluginDiscoverableInfo
import com.prefect47.pluginlib.impl.interfaces.DiscoverableInfo
import com.prefect47.pluginlib.impl.interfaces.PluginInfoFactory
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginInfo
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStore
import javax.inject.Inject

class PluginInfoImpl<T: Plugin> (
    private val discoverableInfo: PluginDiscoverableInfo
): PluginInfo<T> {

    class Factory @Inject constructor(
    ): PluginInfoFactory {
        override fun <T : Plugin> create(discoverableInfo: DiscoverableInfo<T>): PluginInfo<T> =
            PluginInfoImpl(discoverableInfo as PluginDiscoverableInfo)
    }

    companion object {
        private const val TAG = "PluginInfo"
    }

    override val pluginContext = discoverableInfo.context
    override val component = discoverableInfo.component
    override val data = Bundle()

    override fun containsKey(key: String) = discoverableInfo.metadata.containsKey(key)

    override fun getInt(key: String, default: Int) = discoverableInfo.metadata.getInt(key, default)

    override fun getStringResource(key: String, default: String?): String? {
        return try {
            pluginContext.getString(discoverableInfo.metadata.getInt(key))
        } catch (e: Resources.NotFoundException) {
            default
        }
    }

    override fun getDrawableResource(key: String, default: Drawable?): Drawable? {
        return try {
            pluginContext.getDrawable(discoverableInfo.metadata.getInt(key))
        } catch (e: Resources.NotFoundException) {
            default
        }
    }

    override fun onDataStorePreferenceChanged(dataStore: PluginPreferenceDataStore, key: String) {
        Log.w(TAG, "pluginInfo.onDataStorePreferenceChanged NOT IMPLEMENTED")
    }

    override fun start(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
