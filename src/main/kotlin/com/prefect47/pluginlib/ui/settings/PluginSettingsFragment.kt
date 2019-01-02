package com.prefect47.pluginlib.ui.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.plugin.PluginMetadata
import com.prefect47.pluginlib.plugin.PluginSettings

class PluginSettingsFragment : Fragment() {
    private lateinit var metadata: PluginMetadata

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        metadata = PluginLibrary.getMetaData(className!!)!!

        val layoutResId = (metadata.plugin as PluginSettings).layoutResId
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        childFragment.arguments = childFragment.arguments?.let { it.putAll(arguments); it } ?: arguments
        super.onAttachFragment(childFragment)
    }
}
