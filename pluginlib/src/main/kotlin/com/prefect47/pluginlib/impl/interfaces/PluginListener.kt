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

package com.prefect47.pluginlib.impl.interfaces

import com.prefect47.pluginlib.plugin.Plugin

/**
 * Interface for listening to plugins being connected.
 */
interface PluginListener<T: Plugin>: Discoverable.Listener<T> {
    //fun onPluginStarted()

    //fun onPluginStopped()

    /**
     * Called when the item has been loaded and is ready to be used.
     * This may be called multiple times if multiple items are allowed.
     * It may also be called in the future if the item package changes
     * and needs to be reloaded.
     */
    //fun onPluginConnected(info: InstanceManager.DiscoverableInfo<T>)

    /**
     * Called when a item has been uninstalled/updated and should be removed
     * from use.
     */
    //fun onPluginDisconnected(info: InstanceManager.DiscoverableInfo<T>) {
        // Optional.
    //}
}
