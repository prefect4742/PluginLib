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


package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.impl.PluginTrackerFactory
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import kotlin.reflect.KClass

/**
 * Convenience class for plugins to maintain a list of sub-plugins.
 * The returned list will be automatically populated with available plugins of the given type.
 */

@ProvidesInterface(version = PluginTracker.VERSION)
interface PluginTracker {
    companion object {
        const val VERSION = 1

        inline fun <reified T : Plugin> create(p: Plugin): PluginTracker {
            return PluginDependency[p, PluginTrackerFactory::class].create(T::class)
        }

        inline fun <reified T : Plugin> create(cls: KClass<T>): PluginTracker {
            return Dependency[PluginTrackerFactory::class].create(cls)
        }
    }

    suspend fun start()
    fun stop()

    val pluginClass: KClass<*>
    val pluginList: List<Plugin>
}

//typealias PluginTrackerList<T> = ArrayList<Plugin>

/*fun <T: Plugin> PluginTrackerList<T>.getMetaData(): List<Plugin> {
    return map { it }
}
*/
