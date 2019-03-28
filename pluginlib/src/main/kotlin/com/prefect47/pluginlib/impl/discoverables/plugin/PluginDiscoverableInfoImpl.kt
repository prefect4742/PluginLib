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

package com.prefect47.pluginlib.impl.discoverables.plugin

import android.content.ComponentName
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Bundle
import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.DiscoverableManager
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginDiscoverableInfoImpl (
    override val manager: DiscoverableManager<out Discoverable, PluginDiscoverableInfo>,
    override val context: Context,
    override val cls: KClass<out Plugin>,
    override val component: ComponentName,
    override val metadata: Bundle,
    private val pluginInfoFactory: PluginInfoFactory
): PluginDiscoverableInfo {

    class Factory @Inject constructor(
        private val pluginInfoFactory: PluginInfoFactory
    ): PluginDiscoverableInfo.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun create(
            manager: DiscoverableManager<out Discoverable, PluginDiscoverableInfo>,
            discoverableContext: Context,
            cls: KClass<out Discoverable>,
            component: ComponentName,
            serviceInfo: ServiceInfo
        ): PluginDiscoverableInfo {
            val metadata = serviceInfo.metaData ?: Bundle()
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            metadata.putInt(PluginInfo.DESCRIPTION, serviceInfo.descriptionRes)
            metadata.putInt(PluginInfo.ICON, serviceInfo.icon)
            metadata.putInt(PluginInfo.TITLE, serviceInfo.labelRes)
            return PluginDiscoverableInfoImpl(
                manager,
                discoverableContext,
                cls as KClass<out Plugin>,
                component,
                metadata,
                pluginInfoFactory
            )
        }
    }

    override fun <T : Plugin> makePluginInfo(): PluginInfo<T> {
        return pluginInfoFactory.create(this)
    }
}
