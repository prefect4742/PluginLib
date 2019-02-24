package com.prefect47.pluginlib.ui.preference

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.net.Uri
import android.util.AttributeSet
import com.prefect47.pluginlib.R

/**
 * Preference that lets user pick a file from the storage framework.
 */
abstract class PluginFilePickerPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginPreferenceStyle,
    defStyleRes: Int = R.style.PluginPreference
): PluginPreference(context, attrs, defStyleAttr, defStyleRes) {

    private var value: Uri? = null
    private var pickerRequestCode: Int = 0
    val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                 or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                 or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    }

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.PluginFilePickerPreference,
                defStyleAttr, defStyleRes).apply {
                try {
                    pickerRequestCode = getInt(R.styleable.PluginFilePickerPreference_requestCode, 0)
                    if (pickerRequestCode == 0) {
                        throw IllegalStateException("PluginFilePickerPreference must define an android:requestCode")
                    }
                    pickerIntent.type = getString(R.styleable.PluginFilePickerPreference_mimeType) ?: "*/*"
                } finally {
                    recycle()
                }
            }
        }
    }

    fun pickFile() {
        startActivityForResult(pickerIntent, pickerRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == pickerRequestCode && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { uri ->
                // Check for the freshest data.
                val takeFlags: Int = resultData.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                // ...and persist the Uri
                if (callChangeListener(uri)) {
                    setUri(uri)
                }
            }
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val initialValue = getPersistedString(defaultValue as? String)
        initialValue?.let { setUri(Uri.parse(it)) }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    fun setUri(uri: Uri?) {
        if (uri != value) {
            value = uri
            persistString(uri.toString())
            notifyChanged()
        }
    }

    fun getUri() = value
}
