package com.prefect47.pluginlib.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prefect47.pluginlib.impl.Manager
import com.prefect47.pluginlib.plugin.PluginLibraryControl

class PluginListViewModelFactory(
    private val manager: Manager, private val control: PluginLibraryControl
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PluginListViewModelImpl::class.java)) {
            return PluginListViewModelImpl(manager, control) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class @modelClass")
    }
}
