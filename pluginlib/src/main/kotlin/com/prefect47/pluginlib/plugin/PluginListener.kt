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

import android.content.Context

/**
 * Interface for listening to plugins being connected.
 */
interface PluginListener<T : Plugin> {
    /**
     * Called when the plugin has been loaded and is ready to be used.
     * This may be called multiple times if multiple plugins are allowed.
     * It may also be called in the future if the plugin package changes
     * and needs to be reloaded.
     */
    fun onPluginConnected(plugin: T, metadata: PluginMetadata)

    /**
     * Called when a plugin has been uninstalled/updated and should be removed
     * from use.
     */
    fun onPluginDisconnected(plugin: T) {
        // Optional.
    }
}
