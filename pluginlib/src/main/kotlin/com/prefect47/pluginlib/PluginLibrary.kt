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

package com.prefect47.pluginlib

import androidx.fragment.app.FragmentActivity
import com.prefect47.pluginlib.impl.di.PluginLibraryDI

object PluginLibrary {
    const val ARG_CLASSNAME = "pluginClassName"

    fun init(activity: FragmentActivity) {
        PluginLibraryDI.init(activity)
    }

    fun getControl() = PluginLibraryDI.component.getControl()
}
