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

import android.content.Context
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginListener
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.plugin.PluginTracker
import kotlin.reflect.KClass

object PluginTrackerImpl : PluginTracker() {

    class PluginTrackerImpl<T: Plugin>: PluginListener<T>, ArrayList<Entry<T>>() {
        override fun onPluginConnected(plugin: T, metadata: PluginMetadata) {
            Dependency[PluginManager::class].debug("Plugin $plugin connected")
            add(Entry(plugin, metadata))
        }

        override fun onPluginDisconnected(plugin: T) {
            Dependency[PluginManager::class].debug("Plugin $plugin disconnected")
            val iter = listIterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                if (entry.plugin == plugin) {
                    iter.remove()
                    return
                }
            }
        }
    }

    init {
        Dependency[PluginDependencyProvider::class].allowPluginDependency(PluginTracker::class)
    }

    override fun <T : Plugin> create(cls: KClass<T>): List<Entry<T>> {
        val tracker = PluginTrackerImpl<T>()

        // Reload the class with our own classloader
        //val ourCls = Class.forName(cls.qualifiedName).kotlin as KClass<T>

        Dependency[PluginManager::class].addPluginListener(tracker, cls, allowMultiple = true)
        return tracker
    }
}
