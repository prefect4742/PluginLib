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


package com.prefect47.pluginlib.plugin

import com.prefect47.pluginlib.impl.interfaces.InstanceInfo

interface Discoverable {
    /**
     * Behavioral flags that apply to the Discoverable type.
     * Declare these as a companion object member EnumSet called "FLAGS" in your Discoverable class interface.
     */
    enum class Flag {
        /**
         * Allow multiple implementations of this discoverable to be used at the same time.
         */
        ALLOW_SIMULTANEOUS_USE
    }

    interface Listener<T: Discoverable> {
        /**
         * Called when library starts looking for items of the given type. Should be used at application start.
         */
        fun onStartDiscovering()

        /**
         * Called when library has finished loading items of the given type. Should be used at application start.
         */
        fun onDoneDiscovering()

        fun onDiscovered(info: InstanceInfo<T>)

        fun onRemoved(info: InstanceInfo<T>)
    }
}
