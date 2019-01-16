package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.prefect47.pluginlib.R

/**
 * Inline EditText Preference.
 */
class PluginInlineEditTextPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0, defStyleRes: Int = 0) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginListCategory,
                    defStyleAttr, defStyleRes).apply {
                try {
                    /*
                    getString(R.styleable.PluginListCategory_pluginClassName)?.let { className = it }
                    layoutResId = getResourceId(R.styleable.PluginListCategory_pluginEntryLayout,
                            R.layout.plugin_pref)
                            */
                } finally {
                    recycle()
                }
            }
        }
    }
}
