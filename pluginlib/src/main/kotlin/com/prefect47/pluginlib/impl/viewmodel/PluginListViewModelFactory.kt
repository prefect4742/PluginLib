package com.prefect47.pluginlib.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prefect47.pluginlib.impl.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlib.impl.interfaces.Manager
import com.prefect47.pluginlib.plugin.PluginLibraryControl

class PluginListViewModelFactory(
    private val manager: Manager, private val control: PluginLibraryControl,
    private val discoverableInfoFactory: PluginDiscoverableInfo.Factory
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PluginListViewModelImpl::class.java)) {
            return PluginListViewModelImpl(manager, control, discoverableInfoFactory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class @modelClass")
    }
}
