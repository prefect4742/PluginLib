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
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatViewInflater
import androidx.core.view.LayoutInflaterCompat
import java.lang.reflect.Array
import kotlin.reflect.full.declaredFunctions

class DiscoverableContextWrapper(private val appContext: Context, base: Context, private val myClassLoader: ClassLoader,
                                 internal val pkg: String) : ContextWrapper(base) {

    /*
    inner class MyRes: Resources(myAssetManager, appContext.resources.displayMetrics, appContext.resources.configuration) {
        override fun getDrawable(id: Int): Drawable {
            return super.getDrawable(id)
        }

        override fun getDrawable(id: Int, theme: Theme?): Drawable {
            return super.getDrawable(id, theme)
        }

        override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
            return super.getDrawableForDensity(id, density)
        }

        override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
            return super.getDrawableForDensity(id, density, theme)
        }

        override fun getText(id: Int): CharSequence {
            return super.getText(id)
        }

        override fun getText(id: Int, def: CharSequence?): CharSequence {
            return super.getText(id, def)
        }
    }
    */

    private val inflater: LayoutInflater by lazy {
        //AppCompatViewInflater().
        LayoutInflater.from(appContext).cloneInContext(this)
        /*
        val layoutInflater = DiscoverableLayoutInflater(this)
        LayoutInflaterCompat.setFactory2(layoutInflater, DiscoverableLayoutInflaterFactory(this))
        layoutInflater
        */
        //DiscoverableLayoutInflater(LayoutInflater.from(appContext), appContext, this)
    }

    /*
    private val myAssetManager: AssetManager by lazy {
        val assetManager = AssetManager::class.java.newInstance()
        val setApkAssets = assetManager::class.java.getMethod("setApkAssets", Array::class.java, Boolean::class.java)
        val addAssetPath = assetManager::class.java.getMethod("addAssetPath", String::class.java)
        val apkPath = packageResourcePath
        addAssetPath.invoke(assetManager, apkPath)
        assetManager
    }
    */

    private val myResources: Resources by lazy {
        packageManager.getResourcesForApplication(pkg)
        //MyRes()
        /*
        val baseResources = appContext.resources
        Resources(myAssetManager, baseResources.displayMetrics, baseResources.configuration)
        */

        /*
        public PluginContext(Context baseContext, String apkPath, final DexClassLoader dexClassLoader) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        super(baseContext);
        this.dexClassLoader = dexClassLoader;
        assetManager = AssetManager.class.newInstance();
        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
        addAssetPath.invoke(assetManager, apkPath);

        Resources baseResources = baseContext.getResources();
        resources = new Resources(assetManager, baseResources.getDisplayMetrics(), baseResources.getConfiguration());
        theme = resources.newTheme();
        theme.setTo(getTheme());

        layoutInflater = new PluginLayoutInflater(LayoutInflater.from(baseContext), this);



        res
        */
        /*
        val x = packageManager.getResourcesForApplication(pkg)
        x
        */
    }

    override fun getClassLoader(): ClassLoader {
        return myClassLoader
    }

    override fun getTheme(): Resources.Theme {
        return myResources.newTheme()
        /*
        packageManager.getApplicationInfo(pkg, PackageManager.GET_ACTIVITIES).
        return appContext.theme.
                resources.
                */
    }

    override fun getSystemService(name: String): Any {
        if (LAYOUT_INFLATER_SERVICE == name) {
            return inflater
        }
        return baseContext.getSystemService(name) as Any
    }

    override fun getResources(): Resources {
        return myResources
    }

    /*
    override fun getAssets(): AssetManager {
        return myResources.assets
        //return myAssetManager
    }
    */

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences? {
        //val handler = Dependency[Control::class].currentSharedPreferencesHandler
        //handler?.let { return it.getSharedPreferences(name, mode) }

        // If there is no handler set, use common sense.

        // Prevent discoverables from reading anything outside or their own settings
        return super.getSharedPreferences("${pkg}_$name", mode)
    }
}
