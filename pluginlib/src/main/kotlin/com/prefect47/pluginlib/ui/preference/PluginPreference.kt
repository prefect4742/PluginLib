package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import androidx.preference.Preference
import com.prefect47.pluginlib.R

/**
 * Preference that has access to the PreferenceFragment to start activities
 */
open class PluginPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pluginPreferenceStyle,
    defStyleRes: Int = R.style.PluginPreference
): Preference(context, attrs, defStyleAttr, defStyleRes), PluginPreferencesFragment.ActivityResultHandler {

    internal lateinit var parentFragment: PluginPreferencesFragment

    fun startActivityForResult(intent: Intent?, requestCode: Int) {
        parentFragment.startActivityForResult(this, intent, requestCode)
    }

    fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        parentFragment.startActivityForResult(this, intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {}
}
