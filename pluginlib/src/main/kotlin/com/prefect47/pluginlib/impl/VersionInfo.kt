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

import com.prefect47.pluginlib.PluginLibDependenciesImpl
import com.prefect47.pluginlib.plugin.PluginLibDependencies
import com.prefect47.pluginlib.plugin.annotations.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class VersionInfo {
    companion object {
        private var staticPluginDependencies = object: PluginLibDependencies {
            override val providers = mutableMapOf<KClass<*>, PluginLibDependencies.Provider>()
            override val dependencies = mutableMapOf<KClass<*>, List<KClass<*>>>()
        }

        init {
            addStaticDependencies(PluginLibDependenciesImpl)
        }

        internal fun addStaticDependencies(dependencies: PluginLibDependencies) {
            staticPluginDependencies.providers.putAll(dependencies.providers)
            staticPluginDependencies.dependencies.putAll(dependencies.dependencies)
        }

        internal fun getAction(cls: KClass<*>): String? = staticPluginDependencies.providers[cls]?.action
    }

    private val versions: MutableMap<KClass<*>, Version> = HashMap()

    fun hasVersionInfo(): Boolean{
        return !versions.isEmpty()
    }

    fun addClass(cls: KClass<*>): VersionInfo {
        addClass(cls, false)
        return this
    }

    fun addClass(cls: KClass<*>, required: Boolean) {
        if (versions.containsKey(cls)) return

        // Use static data if we have it as it's much faster
        if (staticPluginDependencies.providers.containsKey(cls)) {
            staticPluginDependencies.providers[cls]?.let {
                versions[cls] = Version(it.version, true)
            }

            staticPluginDependencies.dependencies[cls]?.let {
                it.forEach { depCls -> addClass(depCls, true) }
            }
        } else {
            cls.findAnnotation<ProvidesInterface>()?.let { a -> versions[cls] = Version(a.version, true) }
            cls.findAnnotation<DependsOn>()?.let { a -> addClass(a.target, true) }
            cls.findAnnotation<Dependencies>()?.value?.forEach { addClass(it.target, true) }
            cls.findAnnotation<Requires>()?.let { a -> versions[a.target] = Version(a.version, required) }
            cls.findAnnotation<Requirements>()?.value?.forEach { versions[it.target] = Version(it.version, required) }
        }

    }

    @Throws(InvalidVersionException::class)
    fun checkVersion(plugin: VersionInfo) {
        val versionsCopy = HashMap<KClass<*>, Version>(versions)
        plugin.versions.forEach { aClass, version ->
            var v: Version? = versionsCopy.remove(aClass)
            if (v == null) {
                v = createVersion(aClass)
            }
            if (v == null) {
                throw InvalidVersionException(
                    aClass.simpleName
                            + " does not provide an interface", false
                )
            }
            if (v.version != version.version) {
                throw InvalidVersionException(
                    aClass, v.version < version.version, v.version,
                    version.version
                )
            }
        }
        versionsCopy.forEach { aClass, version ->
            if (version.required) {
                throw InvalidVersionException(
                    "Missing required dependency " + aClass.simpleName,
                    false
                )
            }
        }
    }

    private fun createVersion(cls: KClass<*>): Version? {
        var provider: ProvidesInterface? = null
        for (it in cls.annotations) { if (it is ProvidesInterface) { provider = it; break } }
        if (provider != null) {
            return Version(provider.version, false)
        }
        return null
    }

    fun hasClass(cls: KClass<*>): Boolean {
        return versions.containsKey(cls)
    }

    class InvalidVersionException(str: String, val tooNew: Boolean): RuntimeException(str) {
        constructor(cls: KClass<*>, tooNew: Boolean, expected: Int, actual: Int):
                this(cls.simpleName + " expected version " + expected + " but had " + actual, tooNew)
    }

    data class Version(val version: Int, val required: Boolean)
}
