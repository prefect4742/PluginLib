package com.prefect47.pluginlib.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.impl.PluginManager
import com.prefect47.pluginlib.plugin.*
import kotlin.reflect.KClass

class PluginPreferenceCategory : PreferenceCategory {
    private var action: String = "NO_ACTION"
    private var className: String = "NO_CLASSNAME"

    inner class alistener: PluginListener<Plugin> {
        override fun onPluginConnected(plugin: Plugin, pluginContext: Context, metadata: PluginMetadata) {
            Log.d("NIBR", "$key got plugin ${metadata.getTitle()}")
            val pref = SwitchPreference(context)
            //pref.layoutResource = R.layout.plugin_pref
            pref.title = metadata.getTitle()
            pref.summary = metadata.getDescription()
            pref.icon = metadata.getIcon()
            addPreference(pref)
        }

        override fun onPluginDisconnected(plugin: Plugin) {
        }
    }

    private val listener = alistener()

    constructor(context: Context): super(context) {
        init(null, R.attr.preferenceStyle, android.R.attr.preferenceStyle)
    }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init(attrs, R.attr.preferenceStyle, android.R.attr.preferenceStyle)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
    }

    fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.Plugin, defStyleAttr, defStyleRes);
            a.getString(R.styleable.Plugin_action)?.let { action = it }
            a.getString(R.styleable.Plugin_className)?.let { className = it }
        }
        Log.d("NIBR", "Created category with key $key and action $action and className $className")
        val pluginClass = Class.forName(className).kotlin as KClass<Plugin>
        Dependency[PluginManager::class].addPluginListener(listener, pluginClass, action, true)
    }
}
