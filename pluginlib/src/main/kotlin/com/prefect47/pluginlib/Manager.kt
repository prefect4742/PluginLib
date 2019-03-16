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
import com.prefect47.pluginlibimpl.di.PluginLibraryDI
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

        fun getAction(cls: KClass<*>) : String {
            // TODO: Due to KT-7186 we cannot access the members of a subclass inside a loop over the superclass (yet).

            // Try the static dependencies first
            PluginLibraryDI.component.getControl().staticProviders.forEach {
                it.providers[cls]?.let { provider ->
                    return provider.action
                }
            }

            /*
            cls.findAnnotation<ProvidesInterface>()?.let { info ->
                if (TextUtils.isEmpty(info.action)) {
                    throw RuntimeException(cls.simpleName + " doesn't provide an action")
                }
                return info.action
            }
            */

            throw RuntimeException("${cls.simpleName} doesn't provide an interface")
        }

        private var nextNotificationIdInt = 0
        val nextNotificationId: Int
            @Synchronized
            get() = nextNotificationIdInt++
    }

    val discoverableInfoMap: MutableMap<Discoverable, DiscoverableInfo>

    /*
    fun <T: Plugin> getOneShotPlugin(cls: KClass<T>, action: String = getAction(
        cls
    )
    ) : T?
    */

    suspend fun <T: Discoverable, I: DiscoverableInfo> addListener(
        listener: Listener<I>, cls: KClass<T>, action: String = getAction(
            cls
        ), allowMultiple : Boolean = false,
        discoverableInfoFactory: DiscoverableInfo.Factory<I>
    ): DiscoverableManager<T, I>

    fun <I: DiscoverableInfo> removeListener(listener: Listener<I>)

    fun dependsOn(p: Discoverable, cls: KClass<*>) : Boolean

    fun getClassLoader(sourceDir: String, pkg: String): ClassLoader
    fun handleWtfs()

    fun getApplicationContext(): Context

    fun addClassFilter(filter: (String) -> Boolean)
}
