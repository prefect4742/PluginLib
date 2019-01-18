package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R
import com.prefect47.pluginlib.impl.extensions.afterTextChanged
import com.prefect47.pluginlib.impl.extensions.createParcel

/**
 * Inline EditText Preference.
 */
class PluginInlineEditTextPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.editTextPreferenceStyle,
        defStyleRes: Int = android.R.attr.editTextPreferenceStyle)
            : Preference(context, attrs, defStyleAttr, defStyleRes) {
    var editText: AppCompatEditText? = null
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

        layoutResource = R.layout.plugin_pref_inline_edittext
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        editText = (holder?.findViewById(android.R.id.edit) as? AppCompatEditText) ?:
            throw IllegalStateException("PluginInlineEditTextPreference layout must contain an AppCompatEditText with "
                    + "id @android:id/edit")

        editText?.apply {
            inputType = inputTypeAttr
            digitsAttr?.let { keyListener = DigitsKeyListener.getInstance(it) }
            hintAttr?.let { hint = hintAttr }
            setText(value)
            afterTextChanged {
                if (callChangeListener(it)) {
                    setTextInternal(it, false)
                } else {
                    editText?.setText(it)
                }
            }
        }
        editText?.isEnabled = isEnabled
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setText(getPersistedString(defaultValue as? String))
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index) as Any
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

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        val ss = SavedState(superState)
        ss.text = value
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        value = state.text
        notifyChanged()
    }

    class SavedState: BaseSavedState {
        companion object {
            @JvmField @Suppress("unused")
            val CREATOR = createParcel { SavedState(it) }
        }

        var text: String? = null

        @Suppress("unused")
        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(text)
        }

        private constructor(source: Parcel) : super(source) {
            text = source.readString()
        }
    }
}
