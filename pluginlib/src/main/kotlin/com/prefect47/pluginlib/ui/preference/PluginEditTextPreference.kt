package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.prefect47.pluginlib.R

/**
 * EditText Preference wrapper since we need a dialog that has en editText.
 */
class PluginEditTextPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0, defStyleRes: Int = 0) : EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    override fun getDialogLayoutResource(): Int {
        return R.layout.preference_dialog_edittext
    }
}
