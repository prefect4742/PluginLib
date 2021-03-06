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

package com.prefect47.pluginlib.impl.discoverables.factory

import android.content.ComponentName
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Bundle
import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.DiscoverableManager
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverable
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverableInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class FactoryDiscoverableInfoImpl (
    override val manager: DiscoverableManager<out Discoverable, FactoryDiscoverableInfo>,
    override val context: Context,
    override val cls: KClass<FactoryDiscoverable>,
    override val component: ComponentName,
    override val metadata: Bundle
): FactoryDiscoverableInfo {

    override val factory: FactoryDiscoverable by lazy {
        cls.objectInstance ?: cls.createInstance()
    }

    class Factory: FactoryDiscoverableInfo.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun create(
            manager: DiscoverableManager<out Discoverable, FactoryDiscoverableInfo>,
            discoverableContext: Context,
            cls: KClass<out Discoverable>,
            component: ComponentName,
            serviceInfo: ServiceInfo
        ): FactoryDiscoverableInfo {
            val metadata = serviceInfo.metaData ?: Bundle()
            return FactoryDiscoverableInfoImpl(
                manager,
                discoverableContext,
                cls as KClass<FactoryDiscoverable>,
                component,
                //version,
                metadata
            )
        }
    }
}
