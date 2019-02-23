/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.util.ArrayMap
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginDependency.DependencyProvider
import javax.inject.Inject
import kotlin.reflect.KClass

class DependencyProviderImpl @Inject constructor(private val manager: Manager): DependencyProvider() {

    private val dependencies = ArrayMap<Any, Any>()

    fun <T: Any> allowPluginDependency(cls: KClass<T>, obj: T) {
        synchronized (dependencies) {
            dependencies.put(cls, obj)
        }
    }

    override operator fun <T: Any> get(p: Plugin, cls: KClass<T>): T {
        // TODO: Check classloader used on annotations, it should be the system one right?
        // Reload the class with our own classloader
        val ourCls = Class.forName(cls.qualifiedName!!).kotlin
        if (!manager.dependsOn(p, ourCls)) {
            throw IllegalArgumentException(p.javaClass.simpleName + " does not depend on " + cls)
        }
        synchronized (dependencies) {
            if (!dependencies.containsKey(ourCls)) {
                throw IllegalArgumentException("Unknown dependency $cls")
            }
            return dependencies[ourCls] as T
        }
    }
}
