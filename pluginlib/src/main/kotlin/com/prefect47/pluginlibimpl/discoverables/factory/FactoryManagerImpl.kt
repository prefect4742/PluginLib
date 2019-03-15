package com.prefect47.pluginlibimpl.discoverables.factory

import android.util.Log
import com.prefect47.pluginlib.factory.Factory
import com.prefect47.pluginlib.Control
import com.prefect47.pluginlibimpl.DiscoverableManager
import com.prefect47.pluginlibimpl.Manager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FactoryManagerImpl @Inject constructor(
    private val control: Control, private val manager: Manager,
    private val discoverableInfoFactory: FactoryDiscoverableInfo.Factory
): FactoryManager {
    companion object {
        const val TAG = "FactoryManager"
    }

    inner class FactoryAction(val action: String, var discoverableManager: DiscoverableManager<*>?):
        FactoryDiscoverableInfo.Listener {
        override fun onStartDiscovering() {
            control.debug("PluginLib starting tracking factories with ${action}")
        }

        override fun onDoneDiscovering() {
            control.debug("PluginLib started tracking factories with ${action}")
        }

        override fun onDiscovered(info: FactoryDiscoverableInfo) {
            if (control.debugEnabled) Log.d(TAG, "Found factory ${info.component.className}")
        }

        override fun onRemoved(info: FactoryDiscoverableInfo) {
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
                it.discoverableManager = manager.addListener(it, Factory::class, it.action, true,
                    discoverableInfoFactory)
            }
        }

        if (control.debugEnabled) Log.d(TAG, "Started")
    }
}
