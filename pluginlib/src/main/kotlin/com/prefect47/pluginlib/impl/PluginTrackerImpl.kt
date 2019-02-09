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

import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.plugin.PluginTracker.Entry
import kotlin.reflect.KClass

class PluginTrackerImpl<T: Plugin>(override val pluginClass: KClass<*>) :
            PluginTracker, PluginListener<T>, ArrayList<Entry<T>>() {
    companion object Factory: PluginTrackerFactory {
        init {
            Dependency[PluginDependencyProvider::class].allowPluginDependency(PluginTrackerFactory::class)
        }

        override fun <T : Plugin> create(cls: KClass<T>): PluginTracker {
            val tracker = PluginTrackerImpl<T>(cls)
            Dependency[PluginManager::class].addPluginListener(tracker, cls, allowMultiple = true)
            return tracker
        }
    }

    override val pluginList = this

    override fun onPluginConnected(plugin: T, metadata: PluginMetadata) {
        Dependency[PluginLibraryControl::class].debug("Plugin $plugin connected")
        add(Entry(plugin, metadata))
    }

    override fun onPluginDisconnected(plugin: T) {
        Dependency[PluginLibraryControl::class].debug("Plugin $plugin disconnected")
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
