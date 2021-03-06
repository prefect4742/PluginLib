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

import com.prefect47.pluginlib.Control
import com.prefect47.pluginlib.annotations.*
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class VersionInfo(private val control: Control, private val factoryManager: FactoryManager) {
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
                list.providers[cls]?.let { versions[cls] =
                    Version(it.version, true)
                }
                list.dependencies[cls]?.let { it.forEach { depCls -> addClass(depCls, true) } }
                return
            }
        }

        // Use static requirements data if we have it
        factoryManager.findRequirements(cls)?.let { list ->
            list.forEach {
                versions[it.target] = Version(it.version, required)
            }
            return
        }

        // Fallback for interface providers that have no factory.
        control.debug("addClass($cls) reflection fallback, consider generating a factory" )
        cls.findAnnotation<ProvidesInterface>()?.let {
            a -> versions[cls] = Version(a.version, true)
            cls.findAnnotation<DependsOn>()?.let { d -> addClass(d.target, true) }
                ?: cls.findAnnotation<Dependencies>()?.value?.forEach { addClass(it.target, true) }
        }
    }

    @Throws(InvalidVersionException::class)
    fun checkVersion(plugin: VersionInfo) {
        val versionsCopy = HashMap<KClass<*>, Version>(versions)
        plugin.versions.forEach {
            var v: Version? = versionsCopy.remove(it.key)
            if (v == null) {
                v = createVersion(it.key)
            }
            if (v == null) {
                throw InvalidVersionException(
                    "${it.key.simpleName} does not provide an interface", false
                )
            }
            if (v.version != it.value.version) {
                throw InvalidVersionException(
                    it.key.simpleName!!, v.version < it.value.version, v.version,
                    it.value.version
                )
            }
        }
        versionsCopy.forEach {
            if (it.value.required) {
                throw InvalidVersionException(
                    "Missing required dependency ${it.key.simpleName}",
                    false
                )
            }
        }
    }

    private fun createVersion(cls: KClass<*>): Version? {
        control.staticProviders.forEach { list ->
            if (list.providers.containsKey(cls)) {
                list.providers[cls]?.let { return Version(
                    it.version,
                    true
                )
                }
            }
        }
        return null
    }

    fun hasClass(cls: KClass<*>): Boolean {
        return versions.containsKey(cls)
    }

    class InvalidVersionException(str: String, val tooNew: Boolean): RuntimeException(str) {
        constructor(cls: String, tooNew: Boolean, expected: Int, actual: Int):
                this(cls.substringAfterLast("") + " expected version " + expected + " but had " + actual, tooNew)
    }

    data class Version(val version: Int, val required: Boolean)
}
