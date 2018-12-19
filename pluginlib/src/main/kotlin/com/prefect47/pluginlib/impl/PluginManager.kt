/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.text.TextUtils
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginListener
import com.prefect47.pluginlib.plugin.annotations.ProvidesInterface
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface PluginManager {

    interface Factory {
        fun create(
            context: Context,
            defaultHandler: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        ): PluginManager
    }

    companion object {
        lateinit var PLUGIN_PERMISSION: String
        lateinit var CLIENT_PLUGIN_CLASS_PREFIX: String
        const val PLUGIN_CHANGED = "com.prefect47.pluginlib.action.PLUGIN_CHANGED"
        const val DISABLE_PLUGIN = "com.prefect47.pluginlib.action.DISABLE_PLUGIN"
        //const val NOTIFICATION_CHANNEL_TAG = "EXS"
        const val NOTIFICATION_CHANNEL_ID = "PluginManager"

        fun <P: Plugin> getAction(cls: KClass<P>) : String {
            // TODO: Due to KT-7186 we cannot access the members of a subclass inside a loop over the superclass (yet).

            cls.findAnnotation<ProvidesInterface>()?.let { info ->
                if (TextUtils.isEmpty(info.action)) {
                    throw RuntimeException(cls.simpleName + " doesn't provide an action")
                }
                return info.action
            }

            throw RuntimeException(cls.simpleName + " doesn't provide an interface")

            /*
            var info: ProvidesInterface? = null
            for (it in cls.annotations) { if (it is ProvidesInterface) { info = it; break } }

            if (info == null) {
                throw RuntimeException(cls.simpleName + " doesn't provide an interface")
            }
            if (TextUtils.isEmpty(info.action)) {
                throw RuntimeException(cls.simpleName + " doesn't provide an action")
            }
            return info.action
            */
        }

        private var nextNotificationIdInt = 0
        val nextNotificationId: Int
            @Synchronized
            get() = nextNotificationIdInt++
    }

    // must be one of the channels created in NotificationChannels.java

    fun <T: Plugin> getOneShotPlugin(cls: KClass<T>, action: String = getAction(
        cls
    )
    ) : T?

    fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>,
                                      action: String = getAction(
                                          cls
                                      ), allowMultiple : Boolean = false)

    fun removePluginListener(listener: PluginListener<*>)

    fun <T: Any> dependsOn(p: Plugin, cls: KClass<T>) : Boolean

    fun getClassLoader(sourceDir: String, pkg: String): ClassLoader
    fun handleWtfs()

    fun setPermissionName(permissionName: String) {
        PLUGIN_PERMISSION = permissionName
    }

    fun setClientPluginClassPrefix(clientPluginClassPrefix: String) {
        CLIENT_PLUGIN_CLASS_PREFIX = clientPluginClassPrefix
    }
}
