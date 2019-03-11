package com.prefect47.pluginlib.viewmodel

import androidx.lifecycle.LiveData
import com.prefect47.pluginlib.impl.interfaces.InstanceInfo
import com.prefect47.pluginlib.plugin.Plugin

interface PluginListModel<T: Plugin> {
    val plugins: LiveData<List<InstanceInfo<T>>>
}
