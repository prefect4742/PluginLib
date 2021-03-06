package com.prefect47.pluginlib.discoverables.plugin/*
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

/*
package com.prefect47.pluginlib.impl.discoverables.plugin

import com.prefect47.pluginlibimpl.di.PluginLibraryDI
import com.prefect47.pluginlib.annotations.ProvidesInterface
import kotlin.reflect.KClass

@ProvidesInterface(version = PluginDependency.VERSION)
object PluginDependency {
    const val VERSION = 1

    private val provider: DependencyProvider by lazy { PluginLibraryDI.component.getDependencyProvider() }

    operator fun <T: Any> get(p: Plugin, cls: KClass<T>): T {
        return provider[p, cls]
    }

    abstract class DependencyProvider {
        abstract operator fun <T: Any> get(p: Plugin, cls: KClass<T>): T
    }
}
*/
