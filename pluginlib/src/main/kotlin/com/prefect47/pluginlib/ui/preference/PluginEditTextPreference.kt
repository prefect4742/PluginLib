package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.prefect47.pluginlib.R

/**
 * EditText Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 */
class PluginEditTextPreference : EditTextPreference {
    var inputType: Int = InputType.TYPE_CLASS_TEXT
    var digits: String? = null

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
            context.obtainStyledAttributes(attrs, R.styleable.PluginEditTextPreference,
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
        // TODO: Should maybe bet this as an attribute instead?
        return R.layout.preference_dialog_edittext
    }
}
