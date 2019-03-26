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

package com.prefect47.pluginlib

import android.content.Context
import com.prefect47.pluginlib.DiscoverableInfo.Listener
import kotlin.reflect.KClass

interface Manager {

    interface Factory {
        fun create(
            defaultHandler: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        ): Manager
    }

    companion object {
        const val PLUGIN_CHANGED = "com.prefect47.pluginlib.action.PLUGIN_CHANGED"
        const val DISABLE_PLUGIN = "com.prefect47.pluginlib.action.DISABLE_PLUGIN"

        private var nextNotificationIdInt = 0
        val nextNotificationId: Int
            @Synchronized
            get() = nextNotificationIdInt++
    }

    suspend fun <T: Discoverable, I: DiscoverableInfo> addListener(
        listener: Listener<I>, cls: KClass<T>, allowMultiple : Boolean = false,
        discoverableInfoFactory: DiscoverableInfo.Factory<I>
    ): DiscoverableManager<T, I>

    suspend fun <T: Discoverable, I: DiscoverableInfo> addListener(
        listener: Listener<I>, cls: KClass<T>, action: String, allowMultiple : Boolean = false,
        discoverableInfoFactory: DiscoverableInfo.Factory<I>
    ): DiscoverableManager<T, I>

    fun <I: DiscoverableInfo> removeListener(listener: Listener<I>)

    fun dependsOn(discoverableInfo: DiscoverableInfo, cls: KClass<*>): Boolean

    /**
     * Get flags from annotations on the [cls] interface.
     */
    fun <T: Discoverable> getFlags(cls: KClass<T>, flagClass: KClass<*>): Int
    fun getFlags(clsName: String, flagClass: KClass<*>): Int

    fun getClassLoader(sourceDir: String, pkg: String): ClassLoader
    fun handleWtfs()

    fun getApplicationContext(): Context

    fun addClassFilter(filter: (String) -> Boolean)
}
