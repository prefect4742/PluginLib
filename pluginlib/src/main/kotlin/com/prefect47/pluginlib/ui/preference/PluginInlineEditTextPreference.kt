package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.widget.EditText
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.extensions.afterTextChanged

/**
 * Inline EditText Preference.
 */
class PluginInlineEditTextPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.inlineEditTextPreferenceStyle,
    defStyleRes: Int = R.style.PluginInlineEditTextPreference
): PluginPreference(context, attrs, defStyleAttr, defStyleRes) {
    var value: String? = null

    var inputTypeAttr: Int = InputType.TYPE_CLASS_TEXT
    var digitsAttr: String? = null
    var hintAttr: String? = null

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginInlineEditTextPreference,
                    defStyleAttr, defStyleRes).apply {
                try {
                    inputTypeAttr = getInt(R.styleable.PluginInlineEditTextPreference_android_inputType,
                        InputType.TYPE_CLASS_TEXT)
                    digitsAttr = getString(R.styleable.PluginInlineEditTextPreference_android_digits)
                    hintAttr = getString(R.styleable.PluginInlineEditTextPreference_android_hint)
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<EditText>(android.R.id.edit)?.apply {
            inputType = inputTypeAttr
            digitsAttr?.let { keyListener = DigitsKeyListener.getInstance(it) }
            hintAttr?.let { hint = hintAttr }
            setText(value)
            afterTextChanged {
                if (callChangeListener(it)) {
                    setTextInternal(it, false)
                } else {
                    this.setText(it)
                }
            }
            isEnabled = this@PluginInlineEditTextPreference.isEnabled
        } ?: throw IllegalStateException("PluginInlineEditTextPreference layout must contain an AppCompatEditText with "
                        + "id @android:id/edit")
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setText(getPersistedString(defaultValue as? String))
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    fun setText(text: String?) {
        setTextInternal(text, true)
    }

    private fun setTextInternal(text: String?, notifyChanged: Boolean) {
        value = text
        persistString(value)
        if (notifyChanged) {
            notifyChanged()
        }
    }

    fun getText(): String? = value
}
