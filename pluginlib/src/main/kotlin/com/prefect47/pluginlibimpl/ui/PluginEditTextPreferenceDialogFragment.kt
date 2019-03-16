package com.prefect47.pluginlibimpl.ui

import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.EditText
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlibimpl.di.PluginLibraryDI
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo

/**
 * EditTextPreference.
 */
class PluginEditTextPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {
    companion object {
        private const val ARG_INPUTTYPE = "inputType"
        private const val ARG_DIGITS = "digits"

        fun create(className: String, key: String, inputType: Int,
                   digits: String?): PluginEditTextPreferenceDialogFragment =
            PluginEditTextPreferenceDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                    putString(PluginLibrary.ARG_CLASSNAME, className)
                    putInt(ARG_INPUTTYPE, inputType)
                    digits?.let { putString(ARG_DIGITS, digits) }
                }
            }
    }

    private lateinit var pluginInfo: PluginInfo<out Plugin>

    override fun onCreate(savedInstanceState: Bundle?) {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        pluginInfo = PluginLibraryDI.component.getControl().pluginManager[className!!]!!
        super.onCreate(savedInstanceState)
    }

    override fun onBindDialogView(view: View) {
        view.findViewById<EditText>(android.R.id.edit)?.apply {
            inputType = arguments!!.getInt(ARG_INPUTTYPE)
            arguments!!.getString(ARG_DIGITS)?.let { keyListener = DigitsKeyListener.getInstance(it) }
        }
        super.onBindDialogView(view)
    }
}
