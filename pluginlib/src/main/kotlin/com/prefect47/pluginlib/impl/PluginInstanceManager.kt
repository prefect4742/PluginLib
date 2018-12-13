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

import android.content.Context
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginListener
import kotlin.reflect.KClass

interface PluginInstanceManager<T: Plugin> {

    interface Factory {
        fun <T: Plugin> create(
            context: Context, action: String, listener: PluginListener<T>?,
            allowMultiple: Boolean, cls: KClass<*>, manager: PluginManager
        ): PluginInstanceManager<T>
    }

    data class PluginInfo<T: Plugin>(val pkg: String, val cls: String, val plugin: T, val pluginContext: Context,
        val version: VersionInfo?)

    fun loadAll()
    fun checkAndDisable(className: String): Boolean
    fun disableAll(): Boolean
    fun destroy()

    fun getPlugin(): PluginInfo<T>?

    fun <P: Any> dependsOn(p: Plugin, cls: KClass<P>): Boolean

    fun onPackageRemoved(pkg: String)
    fun onPackageChange(pkg: String)
}
