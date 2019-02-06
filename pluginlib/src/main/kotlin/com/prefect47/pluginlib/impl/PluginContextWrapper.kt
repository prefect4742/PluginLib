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

package com.prefect47.pluginlib.impl

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.LayoutInflater
import androidx.preference.PreferenceManager

class PluginContextWrapper(private val appContext: Context, base: Context, private val myClassLoader: ClassLoader,
                           private val pkg: String) : ContextWrapper(base) {
    private val inflater: LayoutInflater by lazy {
        LayoutInflater.from(appContext).cloneInContext(this)
    }

    override fun getClassLoader(): ClassLoader {
        return myClassLoader
    }

    override fun getTheme(): Resources.Theme {
        return appContext.getTheme()
    }

    override fun getSystemService(name: String): Any {
        if (LAYOUT_INFLATER_SERVICE == name) {
            return inflater
        }
        return baseContext.getSystemService(name) as Any
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences? {
        name?.equals("${pkg}_preferences")?.let {
            // TODO Prevent plugins from reading anything outside or their own settings
            return super.getSharedPreferences(name, mode)
        }
        return null
    }
}
