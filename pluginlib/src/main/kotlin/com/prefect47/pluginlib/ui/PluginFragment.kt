package com.prefect47.pluginlib.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.prefect47.pluginlib.impl.di.PluginLibraryDI
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo

open class PluginFragment : Fragment() {
    lateinit var pluginInfo: PluginInfo<out Plugin>
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val className = arguments!!.getString(com.prefect47.pluginlib.PluginLibrary.ARG_CLASSNAME)
        pluginInfo = PluginLibraryDI.component.getControl().pluginManager[className!!]!!
    }

    override fun onAttachFragment(childFragment: Fragment) {
        childFragment.arguments = childFragment.arguments?.also { it.putAll(arguments) } ?: Bundle(arguments)
        super.onAttachFragment(childFragment)
    }
}
