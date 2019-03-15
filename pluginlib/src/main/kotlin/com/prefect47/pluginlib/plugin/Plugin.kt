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
import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager
import com.prefect47.pluginlibimpl.Manager
import com.prefect47.pluginlibimpl.di.PluginLibraryDI
import com.prefect47.pluginlibimpl.discoverables.plugin.PluginDiscoverableInfo

/**
 * Plugins are separate APKs that are expected to implement interfaces
 * provided by your app. Their code is dynamically loaded into the app
 * process and can allow for quick prototyping or extending the func-
 * tionality  of your app.
 *
 * PluginLifecycle:
 * <pre class="prettyprint">
 *
 * plugin.onCreate(Context appContext, Context pluginContext);
 * --- This is always called before any other calls
 *
 * pluginListener.onItemConnected(p: Plugin);
 * --- This lets the plugin hook know that a plugin is now connected.
 *
 * ** Any other calls back and forth between app/plugin **
 *
 * pluginListener.onItemDisconnected(p: Plugin);
 * --- Lets the plugin hook know that it should stop interacting with
 * this plugin and drop all references to it.
 *
 * plugin.onDestroy();
 * --- Finally the plugin can perform any cleanup to ensure that its not
 * leaking into the app process.
 *
 * Any time a plugin APK is updated the plugin is destroyed and recreated
 * to load the new code/resources.
 *
</pre> *
 *
 * Creating plugin hooks:
 *
 * To create a plugin hook, first create an interface in that extends
 * Plugin. Include in it any hooks you want to be able to call into from
 * your app and create callback interfaces for anything you need to
 * pass through into the plugin.
 *
 * Then to attach to any discoverables simply add a plugin listener and
 * onPluginConnected will get called whenever new discoverables are installed,
 * updated, or enabled.  Like this example:
 *
 * <pre class="prettyprint">
 * Manager.getInstance(this).addPluginListener(MyPlugin.COMPONENT,
 * new PluginListener&lt;MyPlugin&gt;() {
 *
 * public void onItemConnected(plugin: MyPlugin) {
 *    ...do some stuff...
 * }
 * }, MyPlugin.VERSION, true /* Allow multiple discoverables *\/);
 *
</pre> *
 * Note the VERSION included here.  Any time incompatible changes in the
 * interface are made, this version should be changed to ensure old discoverables
 * aren't accidentally loaded.  Default implementations can be added for
 * new methods to avoid version changes when possible.
 *
 * Implementing a Plugin:
 *
 * See the ExamplePlugin for an example Android.mk on how to compile
 * a plugin. Note that the base plugin classes is not static for discoverables,
 * the implementation of those classes are provided by pluginlib.
 *
 * Plugin security is based around a signature permission, so discoverables must
 * hold the permission you send to PlugLib.init() in their manifest. You must
 * also declare that permission in your own manifest.
 *
 * <pre class="prettyprint">
 * &lt;uses-permission android:name=&quot;com.yourapp.permission.PLUGIN&quot; /&gt;
 *
</pre> *
 *
 * A plugin is found through a querying for services, so to let your app know
 * about it, create a service with a name that points at your implementation
 * of the plugin interface with the action accompanying it:
 *
 * <pre class="prettyprint">
 * &lt;service android:name=&quot;.TestOverlayPlugin&quot;&gt;
 * &lt;intent-filter&gt;
 * &lt;action android:name=&quot;com.yourapp.action.PLUGIN_COMPONENT&quot; /&gt;
 * &lt;/intent-filter&gt;
 * &lt;/service&gt;
 *
</pre> *
*/
*/

interface Plugin: Discoverable {
    companion object {
        private val manager: Manager by lazy { PluginLibraryDI.component.getManager() }
        private val prefsManager: PluginPreferenceDataStoreManager by lazy { PluginLibraryDI.component.getDataStoreManager() }
    }

    // TODO: Clean up this class.
    // TODO: Much is now in Discoverable.
    // TODO: A Plugin shall be a runnable entity, so one PluginInfo shall map to one Plugin.
    // TODO: And one PluginDiscoverableInfo shall map to multiple PluginInfo.


    /**
     * Behavioral flags that apply to the Plugin type.
     * Declare these as a companion object member EnumSet called "FLAGS" in your Plugin class interface.
     */
    enum class Flag {
        /**
         * Allow multiple implementations of this plugin to be used at the same time. This is just for convenience
         * and will be used in for example the PluginListCategory to choose between single choice and multi choice.
         */
        ALLOW_SIMULTANEOUS_USE
    }

    /*
    val className: String
        get() = this::class.qualifiedName!!
    val pkgName: String
        get() = manager.discoverableInfoMap[this]!!.component.packageName
    */
    val pluginContext: Context
        get() = (manager.discoverableInfoMap[this]!! as PluginDiscoverableInfo).context
    val applicationContext: Context
        get() = manager.getApplicationContext()

    // Called when plugin is loaded, either at app start or when an plugin is installed while app is running.
    // pluginContext and applicationContext is available until onDestroy is called.
    fun onCreate() {}

    // Called when app shuts down, or if APK containing the plugin is uninstalled while app is running.
    fun onDestroy() {}

    // Called when app wishes to use the plugin.
    // preferenceDataStore is available until onStop is called.
    fun onStart() {}

    // Called when app wishes to stop using the plugin.
    // preferenceDataStore may no longer be valid (for example if the app implements a PluginPreferenceDataStoreProvider
    // and has invalidated any currently used PluginPreferenceDataStore discoverables).
    fun onStop() {}

    /*
    val preferenceDataStore: PluginPreferenceDataStore
        get() = null //prefsManager.getPreferenceDataStore(manager.discoverableInfoMap[this]!!)
        */

    // Called when app calls PreferenceDataStoreManager.invalidate(). This can happen if the app implements a
    // PreferenceDataStoreProvider and wishes all its discoverables to switch to another set of preferences.
    // Plugin should reload whatever preferences it has cached.
    fun onPreferenceDataStoreInvalidated() {}
}
