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

import android.graphics.drawable.Drawable
import com.prefect47.pluginlib.impl.interfaces.Manager
import com.prefect47.pluginlib.impl.di.PluginLibraryDI

/*
interface PluginMetaDataImpl: PluginInfo {
    companion object {
        private val manager: Manager by lazy { PluginLibraryDI.component.getManager() }
    }

    // TODO: Factory object
    // TODO: When created, take an applicationInfo object and read metadata from it ?

    override val className: String
    override val pkgName: String

    override val title: String
        get() = pluginContext.getString(manager.instanceInfoMap[this]!!.metadata["titleRes"])
    override val description: String
        get() = pluginContext.getString(manager.instanceInfoMap[this]!!.metadata["descriptionRes"])
    override val icon: Drawable?
        get() = pluginContext.getDrawable(manager.instanceInfoMap[this]!!.metadata["iconRes"])
}
*/
