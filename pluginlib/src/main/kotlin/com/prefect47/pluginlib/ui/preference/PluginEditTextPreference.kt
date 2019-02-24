package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.prefect47.pluginlib.R

/**
 * EditText Preference wrapper since we need a dialog that has en editText.
 */
class PluginEditTextPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginPreferenceStyle,
    defStyleRes: Int = R.style.PluginPreference
): EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {
    var inputType: Int = InputType.TYPE_CLASS_TEXT
    var digits: String? = null

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginEditTextPreference,
                defStyleAttr, defStyleRes).apply {
                try {
                    inputType = getInt(R.styleable.PluginEditTextPreference_android_inputType,
                        InputType.TYPE_CLASS_TEXT)
                    digits = getString(R.styleable.PluginEditTextPreference_android_digits)
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun getDialogLayoutResource(): Int {
        return R.layout.preference_dialog_edittext
    }
}
