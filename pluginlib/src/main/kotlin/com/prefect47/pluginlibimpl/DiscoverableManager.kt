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

package com.prefect47.pluginlibimpl

import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.DiscoverableInfo
import com.prefect47.pluginlib.DiscoverableInfo.Listener
import kotlin.reflect.KClass

interface DiscoverableManager<T: Discoverable> {

    interface Factory {
        fun <T: Discoverable, I: DiscoverableInfo> create(
            action: String, listener: Listener<I>?, allowMultiple: Boolean, cls: KClass<*>,
            discoverableInfoFactory: DiscoverableInfo.Factory<I>
        ): DiscoverableManager<T>
    }

    val cls: KClass<*>
    val discoverables: List<DiscoverableInfo>
    val flags: Set<String>

    suspend fun loadAll()
    fun checkAndDisable(className: String): Boolean
    fun disableAll(): Boolean
    fun destroy()

    //fun getPlugin(): DiscoverableInfo<T>?

    fun dependsOn(p: Discoverable, cls: KClass<*>): Boolean

    fun onPackageRemoved(pkg: String)
    fun onPackageChange(pkg: String)
}
