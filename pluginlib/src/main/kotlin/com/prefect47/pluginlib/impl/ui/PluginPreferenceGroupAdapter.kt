package com.prefect47.pluginlib.impl.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder
import com.prefect47.pluginlib.R
import kotlinx.android.synthetic.main.plugin_pref.view.*

/**
 * Inserts a settings icon into a Preference if if contains a settings_frame element.
 * The key will be passed to the itemView as a tag and can be used anywhere the PreferenceViewHolder is seen.
 */
@SuppressLint("RestrictedApi")
class PluginPreferenceGroupAdapter(preferenceGroup: PreferenceGroup): PreferenceGroupAdapter(preferenceGroup) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)

        holder.itemView.settings_frame?.let {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.plugin_setting, it)
        }

        return holder
    }
}
