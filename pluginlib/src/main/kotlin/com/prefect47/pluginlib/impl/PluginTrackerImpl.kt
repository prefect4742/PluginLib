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
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginTrackerImpl<T: Plugin>(private val manager: PluginManager, private val control: PluginLibraryControl,
            override val pluginClass: KClass<*>) : PluginTracker, PluginListener<T>, ArrayList<Plugin>() {

    class Factory @Inject constructor(private val manager: PluginManager, private val control: PluginLibraryControl,
            private val dependencyProvider: PluginDependencyProvider): PluginTrackerFactory {
        init {
            dependencyProvider.allowPluginDependency(PluginTrackerFactory::class, this)
        }

        override fun <T : Plugin> create(cls: KClass<T>): PluginTracker {
            val tracker = PluginTrackerImpl<T>(manager, control, cls)
            tracker.startFunc = {
                manager.addPluginListener(tracker, cls, allowMultiple = true)
            }
            return tracker
        }
    }

    /*
    companion object Factory: PluginTrackerFactory {
        init {
            Dependency[PluginDependencyProvider::class].allowPluginDependency(PluginTrackerFactory::class)
        }

        override fun <T : Plugin> create(cls: KClass<T>): PluginTracker {
            val tracker = PluginTrackerImpl<T>(cls)
            tracker.startFunc = {
                Dependency[PluginManager::class].addPluginListener(tracker, cls, allowMultiple = true)
            }
            return tracker
        }
    }
    */

    private lateinit var startFunc : suspend () -> Unit
    override val pluginList = this

    override suspend fun start() {
        control.debug("PluginLib starting tracker ${pluginClass.qualifiedName}")
        startFunc.invoke()
        control.debug("PluginLib started tracker ${pluginClass.qualifiedName}")
    }

    override fun stop() {
        manager.removePluginListener(this)
    }

    override fun onPluginConnected(plugin: T) {
        control.debug("Plugin $plugin connected")
        add(plugin)
    }

    override fun onPluginDisconnected(plugin: T) {
        control.debug("Plugin $plugin disconnected")
        val iter = listIterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (entry == plugin) {
                iter.remove()
                return
            }
        }
    }
}
