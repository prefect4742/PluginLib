package com.prefect47.pluginlibimpl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prefect47.pluginlibimpl.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlibimpl.Manager
import com.prefect47.pluginlib.Control

class PluginListViewModelFactory(
    private val manager: Manager, private val control: Control,
    private val discoverableInfoFactory: PluginDiscoverableInfo.Factory
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PluginListViewModelImpl::class.java)) {
            return PluginListViewModelImpl(
                manager,
                control,
                discoverableInfoFactory
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class @modelClass")
    }
}
