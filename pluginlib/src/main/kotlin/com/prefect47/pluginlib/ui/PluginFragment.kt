package com.prefect47.pluginlib.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.impl.Dependency
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginLibraryControl

open class PluginFragment : Fragment() {
    lateinit var plugin: Plugin
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        plugin = Dependency[PluginLibraryControl::class].getPlugin(className!!)!!
    }

    override fun onAttachFragment(childFragment: Fragment) {
        childFragment.arguments = childFragment.arguments?.also { it.putAll(arguments) } ?: Bundle(arguments)
        super.onAttachFragment(childFragment)
    }
}
