package com.prefect47.pluginlib.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.prefect47.pluginlib.R

/**
 * EditText Preference with plugin metadata. Turns on the settings button if plugin has a settings layout.
 */
class PluginEditTextPreference : EditTextPreference {
    constructor(context: Context):
            super(context) {
    }
    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs) {
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun getDialogLayoutResource(): Int {
        // TODO: Should maybe bet this as an attribute instead?
        return R.layout.preference_dialog_edittext
    }
}
