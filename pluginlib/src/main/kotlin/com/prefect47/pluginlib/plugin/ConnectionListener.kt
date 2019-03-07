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

/**
 * Interface for listening to items being connected.
 */
interface ConnectionListener<T> {
    /**
     * Called when library starts looking for items of the given type. Should be used at application start.
     */
    fun onStartLoading()

    /**
     * Called when library has finished loading items of the given type. Should be used at application start.
     */
    fun onDoneLoading()

    /**
     * Called when the item has been loaded and is ready to be used.
     * This may be called multiple times if multiple items are allowed.
     * It may also be called in the future if the item package changes
     * and needs to be reloaded.
     */
    fun onItemConnected(item: T)

    /**
     * Called when a item has been uninstalled/updated and should be removed
     * from use.
     */
    fun onItemDisconnected(item: T) {
        // Optional.
    }
}
