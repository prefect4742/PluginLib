package com.prefect47.pluginlib.ui.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import androidx.preference.Preference

/**
 * Preference that has access to the PreferenceFragment to start activities
 */
open class PluginPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.preferenceStyle,
        defStyleRes: Int = android.R.attr.preferenceStyle)
            : Preference(context, attrs, defStyleAttr, defStyleRes), PluginPreferencesFragment.ActivityResultHandler {

    internal lateinit var parentFragment: PluginPreferencesFragment

    fun startActivityForResult(intent: Intent?, requestCode: Int) {
        parentFragment.startActivityForResult(this, intent, requestCode)
    }

    fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        parentFragment.startActivityForResult(this, intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {}
}
