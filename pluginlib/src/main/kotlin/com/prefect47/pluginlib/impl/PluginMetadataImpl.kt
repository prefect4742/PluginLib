/*
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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import com.prefect47.pluginlib.plugin.PluginMetadata

class PluginMetadataImpl(val pluginContext: Context, val cls: String) : PluginMetadata {

    companion object factory: PluginMetadataFactory {
        override fun create(
            pluginContext: Context, cls: String
        ): PluginMetadata {
            return PluginMetadataImpl(pluginContext, cls)
        }
    }

    private val data: Bundle by lazy {
        val myService = ComponentName(pluginContext, cls);
        pluginContext.getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData
    }

    override fun getTitle(): String? {
        return data.getString("pluginTitle")
    }

    override fun getDescription(): String? {
        return data.getString("pluginDescription")
    }

    override fun getIcon(): Icon? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
