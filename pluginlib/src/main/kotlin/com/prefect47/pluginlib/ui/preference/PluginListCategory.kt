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

class PluginListCategory @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0, defStyleRes: Int = 0) : PreferenceCategory(context, attrs, defStyleAttr, defStyleRes) {
    private var className: String = "NO_CLASSNAME"
    private var layoutResId = R.layout.plugin_pref

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginListCategory,
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
