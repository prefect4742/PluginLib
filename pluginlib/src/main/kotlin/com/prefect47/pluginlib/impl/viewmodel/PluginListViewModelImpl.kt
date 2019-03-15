package com.prefect47.pluginlib.impl.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prefect47.pluginlib.impl.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlib.impl.discoverables.plugin.PluginDiscoverableInfo.Listener
import com.prefect47.pluginlib.plugin.DiscoverableInfo
import com.prefect47.pluginlib.impl.interfaces.InstanceManager
import com.prefect47.pluginlib.impl.interfaces.Manager
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.viewmodel.PluginListModel
import com.prefect47.pluginlib.viewmodel.PluginListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginListViewModelImpl @Inject constructor(
    private val manager: Manager, private val control: PluginLibraryControl,
    private val discoverableInfoFactory: PluginDiscoverableInfo.Factory
): PluginListViewModel, ViewModel() {
    companion object {
        const val TAG = "PluginListViewModel"
    }

    override val list = HashMap<KClass<out Plugin>, PluginListModel<out Plugin>>()

    inner class PluginListModelImpl<T: Plugin>(val cls: KClass<T>) : PluginListModel<T>, Listener {
        override val plugins = MutableLiveData<List<DiscoverableInfo>>()

        private var instanceManager: InstanceManager<T>? = null

        override fun onStartDiscovering() {
            control.debug("PluginLib starting tracking ${cls.qualifiedName}")
        }

        override fun onDoneDiscovering() {
            control.debug("PluginLib started tracking ${cls.qualifiedName}")
        }

        override fun onDiscovered(info: PluginDiscoverableInfo) {
            instanceManager?.let { im -> plugins.postValue(im.discoverables) }
        }

        override fun onRemoved(info: PluginDiscoverableInfo) {
            instanceManager?.let { im -> plugins.postValue(im.discoverables) }
        }

        suspend fun start() {
            instanceManager = manager.addListener(this, cls, allowMultiple = true,
                discoverableInfoFactory = discoverableInfoFactory)
            instanceManager?.let { im -> plugins.postValue(im.discoverables) }
        }

        fun stop() {
            manager.removeListener(this)
        }
    }

    /*
    private fun postList() {
        instanceManager?.let { im -> list.postValue(im.discoverables.map { it.plugin }) }
    }
    */

    override fun track(cls: KClass<out Plugin>) {
        list[cls] = PluginListModelImpl(cls)
    }

    suspend fun start() {
        if (control.debugEnabled) Log.d(TAG, "Starting")
        withContext(Dispatchers.Default) {
            for ((_, model) in list) {
                launch {
                    (model as PluginListModelImpl).start() }
            }
        }
        if (control.debugEnabled) Log.d(TAG, "Started")
    }

    fun stop() {
        for ((_, model) in list) {
            (model as PluginListModelImpl).stop()
        }
    }
}
