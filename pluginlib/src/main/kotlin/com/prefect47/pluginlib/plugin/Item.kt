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

interface PluginLifecycle {
    // Called when item is loaded, either at app start or when an item is installed while app is running.
    // pluginContext and applicationContext is available until onDestroy is called.
    fun onCreate() {}

    // Called when app shuts down, or if APK containing the item is uninstalled while app is running.
    fun onDestroy() {}
}
