package com.prefect47.pluginlib.impl.ui

import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.EditText
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.plugin.PluginMetadata

/**
 * EditTextPreference.
 */
class PluginEditTextPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {
    companion object {
        private const val ARG_INPUTTYPE = "inputType"
        private const val ARG_DIGITS = "digits"

        fun create(className: String, key: String, inputType: Int,
                   digits: String?): PluginEditTextPreferenceDialogFragment {
            val fragment = PluginEditTextPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            b.putString(PluginLibrary.ARG_CLASSNAME, className)
            b.putInt(ARG_INPUTTYPE, inputType)
            digits?.let { b.putString(ARG_DIGITS, digits) }
            fragment.arguments = b
            return fragment
        }
    }

    private lateinit var metadata: PluginMetadata

    override fun onCreate(savedInstanceState: Bundle?) {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        metadata = PluginLibrary.getMetaData(className!!)!!
        super.onCreate(savedInstanceState)
    }

    override fun onBindDialogView(view: View?) {
        view?.findViewById<EditText>(android.R.id.edit)?.apply {
            inputType = arguments!!.getInt(ARG_INPUTTYPE)
            arguments!!.getString(ARG_DIGITS)?.let { keyListener = DigitsKeyListener.getInstance(it) }
        }
        super.onBindDialogView(view)
    }
}
