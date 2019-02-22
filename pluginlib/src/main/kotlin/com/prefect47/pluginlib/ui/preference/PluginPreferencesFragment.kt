package com.prefect47.pluginlib.ui.preference

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.impl.ui.PluginEditTextPreferenceDialogFragment
import com.prefect47.pluginlib.impl.ui.PluginPreferenceAdapter
import com.prefect47.pluginlib.plugin.*

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

    private val prefsManager = Dependency[PluginPreferenceDataStoreManager::class]
    private lateinit var prefs: PluginPreferenceDataStore
    private lateinit var prefsListener: PluginPreferenceDataStore.OnPluginPreferenceDataStoreChangeListener
    private lateinit var plugin: Plugin
    private var activityResultHandler: ActivityResultHandler? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        plugin = PluginLibraryDI.component.getControl().getPlugin(className!!)!!

        prefs = prefsManager.getPreferenceDataStore(plugin)
        preferenceManager.preferenceDataStore = prefs

        val preferencesResId = (plugin as PluginSettings).preferencesResId
        addPreferencesFromResource(preferencesResId)

        prefsListener = plugin as PluginPreferenceDataStore.OnPluginPreferenceDataStoreChangeListener
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return PluginPreferenceAdapter(preferenceScreen, this)
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnPluginPreferenceDataStoreChangeListener(prefsListener)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnPluginPreferenceDataStoreChangeListener(prefsListener)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        val f: DialogFragment
        if (preference is PluginEditTextPreference) {
            f = PluginEditTextPreferenceDialogFragment.create(
                plugin::class.qualifiedName!!,
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
