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

package com.prefect47.pluginlib

import android.content.ComponentName
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Bundle
import kotlin.reflect.KClass

interface DiscoverableInfo {

    interface Listener<I: DiscoverableInfo> {
        /**
         * Called when library starts looking for items of the given type. Should be used at application start.
         */
        fun onStartDiscovering()

        /**
         * Called when library has finished loading items of the given type. Should be used at application start.
         */
        fun onDoneDiscovering()

        fun onDiscovered(info: I)

        fun onRemoved(info: I)
    }

    interface Factory<I: DiscoverableInfo> {
        fun create(
            manager: DiscoverableManager<out Discoverable, I>,
            discoverableContext: Context,
            cls: KClass<out Discoverable>,
            component: ComponentName,
            serviceInfo: ServiceInfo
        ): I
    }

    val manager: DiscoverableManager<out Discoverable, out DiscoverableInfo>
    val context: Context
    val cls: KClass<out Discoverable>
    val component: ComponentName
    val metadata: Bundle
}
