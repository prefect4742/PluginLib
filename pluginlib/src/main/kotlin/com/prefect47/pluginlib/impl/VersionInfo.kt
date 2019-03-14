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

import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.annotations.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class VersionInfo(private val control: PluginLibraryControl) {
    //private val versions: MutableMap<KClass<*>, Version> = HashMap()
    private val versions: MutableMap<KClass<*>, Version> = HashMap()

    fun hasVersionInfo() = !versions.isEmpty()

    fun addClass(cls: KClass<*>): VersionInfo {
        addClass(cls, false)
        return this
    }

    fun addClass(cls: KClass<*>, required: Boolean) {
        if (versions.containsKey(cls)) return

        // Use static providers data if we have it
        control.staticProviders.forEach { list ->
            if (list.providers.containsKey(cls)) {
                list.providers[cls]?.let { versions[cls] = Version(it.version, true) }
                list.dependencies[cls]?.let { it.forEach { depCls -> addClass(depCls, true) } }
                return
            }
        }

        // Use static requirements data if we have it
        control.factories.forEach {
            it.requirements[cls]?.let { list ->
                list.forEach { versions[it.target] = Version(it.version, required) }
                return
            }
        }

        // Most instances will only implement one interface and have one Requires
        // Otherwise they might have more than one Requirements
        // If these are not present, treat the class as a ProvidesInterface and if that exists, check for
        // DependsOn/Dependencies.
        cls.findAnnotation<Requires>()?.let { a -> versions[a.target] = Version(a.version, required) }
            ?: cls.findAnnotation<Requirements>()?.value?.forEach { versions[it.target] = Version(it.version, required) }
            ?: cls.findAnnotation<ProvidesInterface>()?.let { a ->
                versions[cls] = Version(a.version, true)
                cls.findAnnotation<DependsOn>()?.let { d -> addClass(d.target, true) }
                    ?: cls.findAnnotation<Dependencies>()?.value?.forEach { addClass(it.target, true) }
            }
    }

    /*
    fun addClass(cls: KClass<*>): VersionInfo {
        addClass(cls, false)
        return this
    }

    fun addClass(cls: KClass<*>, required: Boolean) {
        if (versions.containsKey(cls)) return

        // Use static providers data if we have it
        control.staticProviders.forEach { list ->
            if (list.providers.containsKey(cls)) {
                list.providers[cls]?.let { versions[cls] = Version(it.version, true) }
                list.dependencies[cls]?.let { it.forEach { depCls -> addClass(depCls, true) } }
                return
            }
        }

        // Use static requirements data if we have it
        control.staticRequirements.forEach {
            it.requirements[cls]?.let { list ->
                list.forEach { versions[it.target] = Version(it.version, required) }
                return
            }
        }

        // Most instances will only implement one interface and have one Requires
        // Otherwise they might have more than one Requirements
        // If these are not present, treat the class as a ProvidesInterface and if that exists, check for
        // DependsOn/Dependencies.
        cls.findAnnotation<Requires>()?.let { a -> versions[a.target] = Version(a.version, required) }
            ?: cls.findAnnotation<Requirements>()?.value?.forEach { versions[it.target] = Version(it.version, required) }
            ?: cls.findAnnotation<ProvidesInterface>()?.let { a ->
                versions[cls] = Version(a.version, true)
                cls.findAnnotation<DependsOn>()?.let { d -> addClass(d.target, true) }
                    ?: cls.findAnnotation<Dependencies>()?.value?.forEach { addClass(it.target, true) }
            }
    }
    */

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
                    "${aClass.simpleName} does not provide an interface", false
                )
            }
            if (v.version != version.version) {
                throw InvalidVersionException(
                    aClass.simpleName!!, v.version < version.version, v.version,
                    version.version
                )
            }
        }
        versionsCopy.forEach { aClass, version ->
            if (version.required) {
                throw InvalidVersionException(
                    "Missing required dependency ${aClass.simpleName}",
                    false
                )
            }
        }
    }

    /*
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
    */

    private fun createVersion(cls: KClass<*>): Version? {
        control.staticProviders.forEach { list ->
            if (list.providers.containsKey(cls)) {
                list.providers[cls]?.let { return Version(it.version, true) }
            }
        }
        return null
    }

    /*
    private fun createVersion(cls: KClass<*>): Version? {
        var provider: ProvidesInterface? = null
        for (it in cls.annotations) { if (it is ProvidesInterface) { provider = it; break } }
        if (provider != null) {
            return Version(provider.version, false)
        }
        return null
    }
    */

    fun hasClass(cls: KClass<*>): Boolean {
        return versions.containsKey(cls)
    }

    /*
    fun hasClass(cls: KClass<*>): Boolean {
        return versions.containsKey(cls)
    }
    */

    class InvalidVersionException(str: String, val tooNew: Boolean): RuntimeException(str) {
        constructor(cls: String, tooNew: Boolean, expected: Int, actual: Int):
                this(cls.substringAfterLast(".") + " expected version " + expected + " but had " + actual, tooNew)
    }

    data class Version(val version: Int, val required: Boolean)
}
