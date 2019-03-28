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

package com.prefect47.pluginlib.impl.discoverables.plugin

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo
import com.prefect47.pluginlib.discoverables.plugin.PluginManager
import dagger.Lazy
import kotlinx.coroutines.*
import javax.inject.Inject

class PluginInfoImpl<T: Plugin> (
    override val discoverableInfo: PluginDiscoverableInfo,
    private val pluginManager: PluginManager,
    private val factoryManager: FactoryManager
): PluginInfo<T> {

    class Factory @Inject constructor(
        private val pluginManagerLazy: Lazy<PluginManager>, private val factoryManager: FactoryManager
    ): PluginInfoFactory {
        private val pluginManager: PluginManager by lazy { pluginManagerLazy.get() }
        override fun <T : Plugin> create(discoverableInfo: PluginDiscoverableInfo): PluginInfo<T> {
            val info = PluginInfoImpl<T>(discoverableInfo, pluginManager, factoryManager)
            pluginManager.hooks.forEach { it.onPluginInfoCreated(info) }
            return info
        }
    }

    companion object {
        private const val TAG = "PluginInfo"
    }

    override val pluginContext = discoverableInfo.context
    override val component = discoverableInfo.component
    override val data = Bundle()
    override val instance: T
        get() = innerInstance ?: throw IllegalStateException("Plugin not started: ${component.className}")

    private var innerInstance: T? = null

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

    @Suppress("UNCHECKED_CAST")
    override suspend fun start(): T {
        if (innerInstance != null) throw IllegalStateException(
            "Attempted to start an already started plugin instance ${component.className}")

        val discoverable = factoryManager.createInstance(discoverableInfo.cls)
            ?: throw IllegalStateException("Failed to start plugin instance ${component.className}")

        innerInstance = discoverable as T
        pluginManager.pluginInfoMap[innerInstance!!] = this
        withContext(Dispatchers.Main) {
            innerInstance!!.onCreate()
        }

        return innerInstance!!
    }

    override suspend fun stop() {
        if (innerInstance == null) throw IllegalStateException(
            "Attempted to stop a not started plugin instance ${component.className}")
        withContext(Dispatchers.Main) {
            innerInstance!!.onDestroy()
        }
        pluginManager.pluginInfoMap.remove(innerInstance!!)
        innerInstance = null
    }
}
