package com.prefect47.pluginlib.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginMetadata

open class PluginFragment : Fragment() {
    lateinit var metadata: PluginMetadata
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        metadata = Dependency[PluginLibraryControl::class].getMetaData(className!!)!!
    }

    override fun onAttachFragment(childFragment: Fragment) {
        childFragment.arguments = childFragment.arguments?.also { it.putAll(arguments) } ?: Bundle(arguments)
        super.onAttachFragment(childFragment)
    }
}
