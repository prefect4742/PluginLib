package com.prefect47.pluginlib.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.PluginLibrary
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.plugin.Plugin

open class PluginFragment : Fragment() {
    lateinit var plugin: Plugin
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val className = arguments!!.getString(PluginLibrary.ARG_CLASSNAME)
        plugin = PluginLibraryDI.component.getControl().getPlugin(className!!)!!
    }

    override fun onAttachFragment(childFragment: Fragment) {
        childFragment.arguments = childFragment.arguments?.also { it.putAll(arguments) } ?: Bundle(arguments)
        super.onAttachFragment(childFragment)
    }
}
