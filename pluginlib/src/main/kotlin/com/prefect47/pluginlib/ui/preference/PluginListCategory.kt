package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginMetadata

/**
 * Preference category that automatically adds all plugins of its type and sets their common layout.
 */

class PluginListCategory @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.preferenceCategoryStyle,
        defStyleRes: Int = android.R.attr.preferenceCategoryStyle)
            : PreferenceCategory(context, attrs, defStyleAttr, defStyleRes) {
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

        val allowMulti = PluginLibrary.getFlags(className)?.contains(Plugin.Flag.ALLOW_SIMULTANEOUS_USE) ?: false
        val creator: (Context, Int, PluginMetadata)-> Preference = if (allowMulti) ::createMultiPref else ::createPref

        PluginLibrary.getMetaDataList(className)?.forEach {
            addPreference(creator(context, layoutResId, it))
        }
    }

    private fun createPref(context: Context, layoutResId: Int, metadata: PluginMetadata) : Preference {
        return PluginListEntry(context, layoutResId, metadata).apply {
            setOnPreferenceChangeListener { preference, newValue ->
                preferenceChanged(preference as PluginListEntry, newValue as Boolean) }
        }
    }

    private fun createMultiPref(context: Context, layoutResId: Int, metadata: PluginMetadata) : Preference {
        return PluginMultiListEntry(context, layoutResId, metadata)
    }

    private fun preferenceChanged(preference: PluginListEntry, newValue: Boolean): Boolean {
        if (newValue) {
            for (index in 0..preferenceCount-1) {
                (getPreference(index) as PluginListEntry).apply {
                    if (this != preference) isChecked = false
                }
            }
            return true
        } else {
            return false
        }
    }
}
