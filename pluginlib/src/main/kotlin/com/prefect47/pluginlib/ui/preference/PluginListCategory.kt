package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.plugin.Plugin

/**
 * Preference category that automatically adds all plugins of its type and sets their common layout.
 */

class PluginListCategory @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginListCategoryStyle,
    defStyleRes: Int = R.style.PluginListCategory
): PreferenceCategory(context, attrs, defStyleAttr, defStyleRes) {
    private var className: String = "NO_CLASSNAME"
    private var layoutResId = R.layout.plugin_pref

    interface SettingsHandler {
        fun openSettings(plugin: Plugin)
    }

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginListCategory,
                    defStyleAttr, defStyleRes).apply {
                try {
                    getString(R.styleable.PluginListCategory_pluginClassName)?.let { className = it }
                    layoutResId = getResourceId(R.styleable.PluginListCategory_pluginEntryLayout,
                            0)
                    if (layoutResId == 0) {
                        layoutResId = R.layout.plugin_pref
                    }
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        val control = PluginLibraryDI.component.getControl()
        val allowMulti = control.getFlags(className)?.contains(Plugin.Flag.ALLOW_SIMULTANEOUS_USE) ?: false
        val creator: (Context, Int, Plugin)-> Preference = if (allowMulti) ::createMultiPref else ::createPref

        control.getPluginList(className)?.forEach {
            addPreference(creator(context, layoutResId, it))
        }
    }

    private fun createPref(context: Context, layoutResId: Int, plugin: Plugin) =
        PluginSingleListEntry(context, layoutResId, plugin).apply {
            setOnPreferenceChangeListener { preference, newValue ->
                preferenceChanged(preference as PluginSingleListEntry, newValue as Boolean) }
        }

    private fun createMultiPref(context: Context, layoutResId: Int, plugin: Plugin) =
        PluginMultiListEntry(context, layoutResId, plugin)

    private fun preferenceChanged(preference: PluginSingleListEntry, newValue: Boolean): Boolean {
        return if (newValue) {
            for (index in 0 until preferenceCount) {
                (getPreference(index) as PluginSingleListEntry).apply {
                    if (this != preference) isChecked = false
                }
            }
            true
        } else {
            false
        }
    }
}
