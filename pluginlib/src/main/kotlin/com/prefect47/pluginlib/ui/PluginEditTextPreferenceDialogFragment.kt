package com.prefect47.pluginlib.ui

import android.os.Bundle
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.plugin.PluginMetadata

/**
 * EditTextPreference.
 */
class PluginEditTextPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {
    companion object {
        fun create(className: String, key: String): PluginEditTextPreferenceDialogFragment {
            val fragment = PluginEditTextPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            b.putString(PluginLibrary.ARG_CLASSNAME, className)
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
}
