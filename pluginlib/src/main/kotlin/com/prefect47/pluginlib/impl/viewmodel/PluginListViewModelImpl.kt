package com.prefect47.pluginlib.impl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prefect47.pluginlib.impl.interfaces.InstanceManager
import com.prefect47.pluginlib.impl.interfaces.Manager
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.impl.interfaces.PluginListener
import com.prefect47.pluginlib.plugin.PluginInfo
import com.prefect47.pluginlib.viewmodel.PluginListModel
import com.prefect47.pluginlib.viewmodel.PluginListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginListViewModelImpl @Inject constructor(
    private val manager: Manager, private val control: PluginLibraryControl
): PluginListViewModel, ViewModel() {
    override val list = HashMap<KClass<out Plugin>, PluginListModel<out Plugin>>()

    inner class PluginListModelImpl<T: Plugin>(val cls: KClass<T>) : PluginListModel<T>, PluginListener<T> {
        override val plugins = MutableLiveData<List<PluginInfo<T>>>()

        private var instanceManager: InstanceManager<T>? = null

        override fun onStartLoading() {
            control.debug("PluginLib starting tracking ${cls.qualifiedName}")
        }

        override fun onDoneLoading() {
            control.debug("PluginLib started tracking ${cls.qualifiedName}")
        }

        override fun onPluginDiscovered(info: InstanceManager.InstanceInfo<T>) {
            instanceManager?.let { im -> plugins.postValue(im.instances.map { it.info }) }
        }

        override fun onPluginRemoved(info: InstanceManager.InstanceInfo<T>) {
            instanceManager?.let { im -> plugins.postValue(im.instances.map { it.info }) }
        }

        suspend fun start() {
            instanceManager = manager.addPluginListener(this, cls, allowMultiple = true)
            instanceManager?.let { im -> plugins.postValue(im.instances.map { it.info }) }
        }

        fun stop() {
            manager.removePluginListener(this)
        }
    }

    /*
    private fun postList() {
        instanceManager?.let { im -> list.postValue(im.instances.map { it.plugin }) }
    }
    */

    override fun track(cls: KClass<out Plugin>) {
        list[cls] = PluginListModelImpl(cls)
    }

    suspend fun start() {
        withContext(Dispatchers.Default) {
            for ((_, model) in list) {
                launch {
                    (model as PluginListModelImpl).start() }
            }
        }
    }

    fun stop() {
        for ((_, model) in list) {
            (model as PluginListModelImpl).stop()
        }
    }
}
