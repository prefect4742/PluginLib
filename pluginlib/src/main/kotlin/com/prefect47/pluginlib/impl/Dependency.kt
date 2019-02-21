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

import android.content.Context
import android.util.ArrayMap
import com.prefect47.pluginlib.impl.di.component.DaggerPluginLibraryComponent
import com.prefect47.pluginlib.impl.di.component.PluginLibraryComponent
import com.prefect47.pluginlib.impl.di.module.PluginLibraryModule
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager
import kotlin.reflect.KClass

/**
 * Class to handle ugly dependencies throughout pluginlib until we determine the
 * long-term dependency injection solution.
 *
 * Classes added here should be things that are expected to live the lifetime of
 * pluginlib, and are generally applicable to many parts of pluginlib. They will be
 * lazily initialized to ensure they aren't created on form factors that don't need
 * them.
 * Despite being lazily initialized, it is expected that all dependencies will be
 * gotten during pluginlib startup, and not during runtime to avoid jank.
 *
 * All classes used here are expected to manage their own lifecycle, meaning if
 * they have no clients they should not have any registered resources like bound
 * services, registered receivers, etc.
 */
object Dependency {

    lateinit var component: PluginLibraryComponent

    class DependencyProvider<T>( val createDependency: () -> T )

    class DependencyKey<V>(private val displayName: String) {
        override fun toString(): String = displayName
    }

    /**
     * Checks to see if a dependency is instantiated, if it is it removes it from
     * the cache and calls the destroy callback.
     */
    /*
    fun <T> destroy(cls: Class<T>, destroy: Consumer<T>) {
        instance.destroyDependency(cls, destroy)
    }
    */

    private val dependencies = ArrayMap<Any, Any>()
    private val providers = ArrayMap<Any, DependencyProvider<*>>()
    //private val components

    fun init(context: Context) {
        start(context)
    }

    operator fun <T: Any> get(cls: KClass<T>): T = getDependencyInner(cls)

    operator fun <T: Any> get(cls: DependencyKey<T>): T =
        getDependencyInner(cls)

    operator fun <T: Any> set(cls: Any, value: T) {
        setDependencyInner(cls, value)
    }

    @Synchronized
    fun start(context: Context) {
        if (providers.isNotEmpty()) {
            throw IllegalStateException("Can only be called once")
        }

        component = DaggerPluginLibraryComponent.builder().context(context).build()

        /*
        providers[PluginPrefs::class] =
                DependencyProvider {
                    component.getPluginPrefs()
                }
        */

        providers[PluginManager::class] =
                DependencyProvider {
                    component.getPluginManager()
                }

        providers[PluginLibraryControl::class] =
                DependencyProvider {
                    component.getPluginLibraryControl()
                }

        providers[PluginTrackerFactory::class] =
                DependencyProvider {
                    PluginTrackerImpl.Factory
                }

        providers[PluginDependencyProvider::class] =
                DependencyProvider {
                    PluginDependencyProvider.init(
                        get(
                            PluginManager::class
                        )
                    )
                    PluginDependencyProvider
                }

        /*
        providers[PluginInstanceManager.Factory::class] =
                DependencyProvider {
                    component.getPluginInstanceManagerFactory()
                    //PluginInstanceManagerImpl.Factory
                }
        */

        providers[PluginPreferenceDataStoreManager::class] =
                DependencyProvider {
                    PluginPreferenceDataStoreManagerImpl.init(
                        PluginDefaultPreferenceDataStoreProvider
                    )
                    PluginPreferenceDataStoreManagerImpl
                }

        // TODO: Allow app that uses library to add its own dependencies
        /*
        providers.put(PluginDependencyProvider::class,
                DependencyProvider { PluginDependencyProvider(get(PluginManager::class)) })
        */
    }

    @Synchronized
    private fun <T: Any> getDependencyInner(key: Any): T {
        var obj: Any? = dependencies[key]
        if (obj == null) {
            obj = createDependency<T>(key)
            dependencies[key] = obj
        }
        @Suppress("UNCHECKED_CAST")
        return obj as T
    }

    @Synchronized
    private fun <T: Any> setDependencyInner(key: Any, value: T) {
        if (!dependencies.containsKey(key)) {
            dependencies[key] = value
        }
    }

    private fun <T: Any> createDependency(cls: Any): T {
        if (cls !is DependencyKey<*> && cls !is KClass<*>) {
            throw IllegalArgumentException()
        }

        val provider = providers[cls] ?: throw IllegalArgumentException(
            "Unsupported dependency $cls. ${providers.size} providers known."
        )
        @Suppress("UNCHECKED_CAST")
        return provider.createDependency.invoke() as T
    }
}
