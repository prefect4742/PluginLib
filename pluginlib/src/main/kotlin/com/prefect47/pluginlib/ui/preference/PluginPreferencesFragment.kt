package com.prefect47.pluginlib.ui.preference

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStore
import com.prefect47.pluginlibimpl.di.PluginLibraryDI
import com.prefect47.pluginlibimpl.ui.PluginEditTextPreferenceDialogFragment
import com.prefect47.pluginlibimpl.ui.PluginPreferenceAdapter
import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.settings.PluginSettingsFragment
import java.lang.IllegalArgumentException

/**
 * Settings fragment that inflates a preference XML resource owned by the plugin.
 */
class PluginPreferencesFragment : PreferenceFragmentCompat() {
    interface ActivityResultHandler {
        fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?)
    }

    companion object {
        const val DIALOG_FRAGMENT_TAG = "com.prefect47.pluginlib.ui.PreferenceFragment.DIALOG"
    }

    private lateinit var prefs: PluginPreferenceDataStore
    private lateinit var pluginInfo: PluginInfo<out Plugin>
    private var activityResultHandler: ActivityResultHandler? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        pluginInfo = PluginLibraryDI.component.getControl().getPlugin(className!!)!!
        val parent = parentFragment
        if (parent is PluginSettingsFragment) {
            parent.onPluginInfoCreated(pluginInfo)
        }

        prefs = PluginLibraryDI.component.getDataStoreManager().getPreferenceDataStore(pluginInfo)
        preferenceManager.preferenceDataStore = prefs

        val preferencesResId = pluginInfo.getInt(PluginSettings.PREFERENCES, 0)
        if (preferencesResId == 0) {
            throw IllegalArgumentException("${pluginInfo.component.className} missing preferences resource meta-data")
        }
        addPreferencesFromResource(preferencesResId!!)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return PluginPreferenceAdapter(preferenceScreen, this)
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnPluginPreferenceDataStoreChangeListener(pluginInfo)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnPluginPreferenceDataStoreChangeListener(pluginInfo)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        val f: DialogFragment
        if (preference is PluginEditTextPreference) {
            f = PluginEditTextPreferenceDialogFragment.create(
                pluginInfo.component.className,
                preference.key,
                preference.inputType,
                preference.digits

            )
            f.setTargetFragment(this, 0)
            f.show(fragmentManager!!,
                DIALOG_FRAGMENT_TAG
            )
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    internal fun startActivityForResult(handler: ActivityResultHandler, intent: Intent?, requestCode: Int) {
        activityResultHandler = handler
        super.startActivityForResult(intent, requestCode)
    }

    internal fun startActivityForResult(handler: ActivityResultHandler, intent: Intent?, requestCode: Int,
            options: Bundle?) {
        activityResultHandler = handler
        super.startActivityForResult(intent, requestCode, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultHandler?.onActivityResult(requestCode, resultCode, data)
        activityResultHandler = null
    }
}
