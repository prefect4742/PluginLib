package com.prefect47.pluginlib.impl.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.ui.preference.PluginEditTextPreference

/**
 * Settings fragment that inflates a preference XML resource owned by the plugin.
 */
class PluginWrapperFragment : PreferenceFragmentCompat() {
    companion object {
        val DIALOG_FRAGMENT_TAG = "com.prefect47.pluginlib.ui.PreferenceFragment.DIALOG"
    }

    private lateinit var metadata: PluginMetadata

    @SuppressLint("RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        metadata = PluginLibrary.getMetaData(className!!)!!

        preferenceManager.sharedPreferencesName = "${className}_preferences"

        val xmlRoot = preferenceManager.inflateFromResource(
            metadata.pluginContext, metadata.getSettingsResId(), null) as PreferenceScreen

        val root: Preference
        if (rootKey != null) {
            root = xmlRoot.findPreference(rootKey)
            if (root !is PreferenceScreen) {
                throw IllegalStateException("Preference object with key $rootKey is not a PreferenceScreen")
            }
        } else {
            root = xmlRoot
        }

        preferenceScreen = root
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        val f: DialogFragment
        if (preference is PluginEditTextPreference) {
            f = PluginEditTextPreferenceDialogFragment.create(
                metadata.className,
                preference.key
            );
            f.setTargetFragment(this, 0);
            f.show(fragmentManager!!,
                DIALOG_FRAGMENT_TAG
            );
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}