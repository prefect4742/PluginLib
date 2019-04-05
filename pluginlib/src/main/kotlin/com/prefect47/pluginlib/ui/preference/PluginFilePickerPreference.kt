package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.prefect47.pluginlib.R

/**
 * Preference that lets user pick a file from the storage framework.
 */
abstract class PluginFilePickerPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginPreferenceStyle,
    defStyleRes: Int = R.style.PluginPreference
): PluginFilePickerPreferenceBase(context, attrs, defStyleAttr, defStyleRes) {
    init {
        pickerIntent.action = Intent.ACTION_OPEN_DOCUMENT
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginFilePickerPreference,
                defStyleAttr, defStyleRes).apply {
                try {
                    pickerIntent.type = getString(R.styleable.PluginFilePickerPreference_mimeType) ?: "*/*"
                } finally {
                    recycle()
                }
            }
        }
    }
}
