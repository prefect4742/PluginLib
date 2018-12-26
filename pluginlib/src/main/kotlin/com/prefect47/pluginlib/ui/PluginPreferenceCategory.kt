package com.prefect47.pluginlib.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.R

class PluginPreferenceCategory : PreferenceCategory {
    private var className: String = "NO_CLASSNAME"

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
            a.getString(R.styleable.Plugin_className)?.let { className = it }
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        val metadata = PluginLibrary.getMetaData(className)
        metadata?.forEach {
            val pref = SwitchPreference(context)
            //pref.layoutResource = R.layout.plugin_pref
            pref.title = it.getTitle()
            pref.summary = it.getDescription()
            pref.icon = it.getIcon()
            addPreference(pref)
        }
    }
}
