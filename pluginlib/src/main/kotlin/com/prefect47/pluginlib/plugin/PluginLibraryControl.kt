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

package com.prefect47.pluginlib.plugin

import android.util.Log
import com.prefect47.pluginlib.ui.preference.PluginSettingsEntrance
import java.util.EnumSet
import kotlin.reflect.KClass

interface PluginLibraryControl {
    companion object {
        const val DEFAULT_PERMISSIONNAME = "com.prefect47.pluginlib.permission.PLUGIN"
        const val DEFAULT_DEBUGTAG = "PluginLib"
    }

    interface StateListener {
        fun onStarted() // All added trackers have loaded their instances
        fun onPaused()
        fun onResumed()
        fun onStopped()
    }

    val staticProviders: List<PluginLibProviders>
    val staticImplementations: List<PluginLibImplementations>
    val staticRequirements: List<PluginLibRequirements>
    val factories: List<PluginFactory>

    var settingsHandler: PluginSettingsEntrance.Callback?

    /**
     * Set this if you wish for the library to look for PluginFactory in APK:s.
     */
    //var pluginFactoryName: String?

    var permissionName: String
    var debugEnabled: Boolean
    var debugTag: String
    var notificationChannel: String?
    var notificationIconResId: Int

    val preferenceDataStoreManager: PluginPreferenceDataStoreManager

    //val viewModel: PluginListViewModel

    fun addStaticProviders(providers: PluginLibProviders)

    fun addStaticImplementations(implementations: PluginLibImplementations)
    fun removeStaticImplementations(implementations: PluginLibImplementations)
    fun addStaticRequirements(requirements: PluginLibRequirements)
    fun removeStaticRequirements(requirements: PluginLibRequirements)

    fun addFactory(factory: PluginFactory)
    fun removeFactory(factory: PluginFactory)

    /*
    suspend fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>,
        action: String = Manager.getAction(cls), allowMultiple : Boolean = false)
    fun removePluginListener(listener: PluginListener<*>)
    */

    fun track(factoryAction: String)
    fun track(cls: KClass<out Plugin>)

    fun addStateListener(listener: StateListener)
    fun removeStateListener(listener: StateListener)

    suspend fun start()
    fun pause()
    fun resume()
    fun stop()

    suspend fun startPlugin(plugin: Plugin)
    suspend fun stopPlugin(plugin: Plugin)

    /**
     * Add a [filter] to the plugin classloader that lets instances use libraries or code where class names might
     * conflict with those of the app.
     */
    fun addClassFilter(filter: (String) -> Boolean)

    /**
     * Get a list of PluginInfo containers for any plugin implementing the [pluginClass] interface.
     * Note that each call to this will return new instances of the containers.
     */
    fun getPluginList(pluginClass: KClass<out Plugin>): List<PluginInfo<out Plugin>>?
    @Suppress("UNCHECKED_CAST")
    fun getPluginList(pluginClassName: String) =
            getPluginList(Class.forName(pluginClassName).kotlin as KClass<out Plugin>)

    fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>?

    /**
     * Get a PluginInfo container for the plugin [cls].
     * Note that each call to this will return a new instance of the container.
     */
    fun getPlugin(cls: KClass<out Plugin>) = getPlugin(cls.qualifiedName!!)
    fun getPlugin(className: String): PluginInfo<out Plugin>?

    fun debug(msg: String) {
        if (debugEnabled) Log.d(debugTag, msg)
    }
}
