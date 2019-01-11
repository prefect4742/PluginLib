package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.R

/**
 * Preference category that automatically adds all plugins of its type and sets their common layout.
 */
class PluginListCategory : PreferenceCategory {
    private var className: String = "NO_CLASSNAME"
    private var layoutResId = R.layout.plugin_pref

    constructor(context: Context):
            super(context) {
        init(null, R.attr.preferenceStyle, android.R.attr.preferenceStyle)
    }
    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs) {
        init(attrs, R.attr.preferenceStyle, android.R.attr.preferenceStyle)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.PluginListCategory,
                    defStyleAttr, defStyleRes).apply {
                try {
                    getString(R.styleable.PluginListCategory_pluginClassName)?.let { className = it }
                    layoutResId = getResourceId(R.styleable.PluginListCategory_pluginEntryLayout,
                            R.layout.plugin_pref)
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        PluginLibrary.getMetaDataList(className)?.forEach {
            addPreference(PluginListEntry(context, layoutResId, it))
        }
    }
}
