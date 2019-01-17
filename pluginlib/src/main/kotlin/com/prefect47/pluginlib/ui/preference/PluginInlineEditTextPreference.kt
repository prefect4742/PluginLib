package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R

/**
 * Inline EditText Preference.
 */
class PluginInlineEditTextPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.editTextPreferenceStyle,
        defStyleRes: Int = android.R.attr.editTextPreferenceStyle)
            : Preference(context, attrs, defStyleAttr, defStyleRes) {
    var titleAttr: String? = null
    var inputTypeAttr: Int = InputType.TYPE_CLASS_TEXT
    var digitsAttr: String? = null
    var defaultValueAttr: String? = null
    var hintAttr: String? = null

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginInlineEditTextPreference,
                    defStyleAttr, defStyleRes).apply {
                try {
                    titleAttr = getString(R.styleable.PluginInlineEditTextPreference_android_title)
                    inputTypeAttr = getInt(R.styleable.PluginInlineEditTextPreference_android_inputType,
                        InputType.TYPE_CLASS_TEXT)
                    digitsAttr = getString(R.styleable.PluginInlineEditTextPreference_android_digits)
                    defaultValueAttr = getString(R.styleable.PluginInlineEditTextPreference_android_defaultValue)
                    hintAttr = getString(R.styleable.PluginInlineEditTextPreference_android_hint)
                } finally {
                    recycle()
                }
            }
        }

        layoutResource = R.layout.plugin_pref_inline_edittext
        //widgetLayoutResource = R.layout.plugin_pref_widget_edittext
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        (holder?.findViewById(R.id.plugin_pref_inline_edittext_value) as? AppCompatEditText)?.apply {
            inputType = inputTypeAttr
            digitsAttr?.let { keyListener = DigitsKeyListener.getInstance(it) }
            defaultValueAttr?.let { setText(it) }
            hintAttr?.let { hint = hintAttr }
        }
    }
}
