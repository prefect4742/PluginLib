package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

/**
 * Preference category that automatically adds all plugins of its type and sets their common layout.
 */
abstract class PluginPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.preferenceStyle,
        defStyleRes: Int = android.R.attr.preferenceStyle)
            : Preference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        init(attrs, defStyleAttr, defStyleRes)
    }

    protected abstract fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
}
