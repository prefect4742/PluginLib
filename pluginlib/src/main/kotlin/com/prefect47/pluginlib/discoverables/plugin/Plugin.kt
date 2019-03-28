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


package com.prefect47.pluginlib.discoverables.plugin

import android.content.Context
import com.prefect47.pluginlib.Discoverable
import com.prefect47.pluginlib.annotations.ProvidesInterface
import com.prefect47.pluginlib.impl.di.PluginLibraryDI

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

@ProvidesInterface(version = Plugin.VERSION)
interface Plugin: Discoverable {
    companion object {
        const val VERSION = 1

        /**
         * Allow multiple implementations of this plugin to be used at the same time. This is just for convenience
         * and will be used in for example the PluginListCategory to choose between single choice and multi choice.
         */
        const val FLAG_ALLOW_SIMULTANEOUS_USE = 1
    }

    val pluginContext: Context
        get() = (PluginLibraryDI.component.getControl().pluginManager.pluginInfoMap[this]!!).pluginContext
    val applicationContext: Context
        get() = PluginLibraryDI.component.getControl().manager.getApplicationContext()

    // Called when an instance of plugin is started.
    // pluginContext and applicationContext is available until onDestroy has been called.
    fun onCreate() {}

    // Called when instance of plugin is stopped.
    fun onDestroy() {}

    val preferenceDataStore: com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
        get() = PluginLibraryDI.component.getControl().preferenceDataStoreManager.getPreferenceDataStore(
            PluginLibraryDI.component.getControl().pluginManager.pluginInfoMap[this]!!)

    // Called when app calls PreferenceDataStoreManager.invalidate(). This can happen if the app implements a
    // PreferenceDataStoreProvider and wishes all its discoverables to switch to another set of preferences.
    // Plugin should reload whatever preferences it has cached.
    fun onPreferenceDataStoreInvalidated() {}
}
