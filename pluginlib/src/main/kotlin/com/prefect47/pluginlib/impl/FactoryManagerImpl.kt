package com.prefect47.pluginlib.impl

import android.util.Log
import com.prefect47.pluginlib.impl.interfaces.*
import com.prefect47.pluginlib.plugin.Discoverable
import com.prefect47.pluginlib.plugin.PluginFactory
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FactoryManagerImpl @Inject constructor(
    private val control: PluginLibraryControl, private val manager: Manager
): FactoryManager {
    companion object {
        const val TAG = "FactoryManager"
    }

    inner class FactoryAction(val action: String, var instanceManager: InstanceManager<*>?): Discoverable.Listener<PluginFactory> {
        override fun onStartDiscovering() {
            control.debug("PluginLib starting tracking factories with ${action}")
        }

        override fun onDoneDiscovering() {
            control.debug("PluginLib started tracking factories with ${action}")
        }

        override fun onDiscovered(info: InstanceInfo<PluginFactory>) {
            if (control.debugEnabled) Log.d(TAG, "Found factory ${info.component.className}")
        }

        override fun onRemoved(info: InstanceInfo<PluginFactory>) {
            if (control.debugEnabled) Log.d(TAG, "Factory ${info.component.className} was removed")
        }
    }

    private val factoryActions = ArrayList<FactoryAction>()

    override fun track(action: String) {
        factoryActions.add(FactoryAction(action, null))
    }

    override suspend fun start() {
        if (control.debugEnabled) Log.d(TAG, "Starting")

        withContext(Dispatchers.Default) {
            factoryActions.forEach {
                it.instanceManager = manager.addListener(it, PluginFactory::class, it.action, true)
            }
        }

        if (control.debugEnabled) Log.d(TAG, "Started")
    }
}
