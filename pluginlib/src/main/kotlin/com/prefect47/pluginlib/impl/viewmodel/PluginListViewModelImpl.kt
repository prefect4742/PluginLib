package com.prefect47.pluginlib.impl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prefect47.pluginlib.impl.InstanceManager
import com.prefect47.pluginlib.impl.Manager
import com.prefect47.pluginlib.plugin.Plugin
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginListener
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
        override val plugins = MutableLiveData<List<T>>()

        private var instanceManager: InstanceManager<T>? = null

        override fun onStartLoading() {
            control.debug("PluginLib starting tracking ${cls.qualifiedName}")
        }

        override fun onDoneLoading() {
            control.debug("PluginLib started tracking ${cls.qualifiedName}")
        }

        override fun onPluginConnected(plugin: T) {
            instanceManager?.let { im -> plugins.postValue(im.plugins.map { it.plugin }) }
        }

        override fun onPluginDisconnected(plugin: T) {
            instanceManager?.let { im -> plugins.postValue(im.plugins.map { it.plugin }) }
        }

        suspend fun start() {
            instanceManager = manager.addPluginListener(this, cls, allowMultiple = true)
            instanceManager?.let { im -> plugins.postValue(im.plugins.map { it.plugin }) }
        }

        fun stop() {
            manager.removePluginListener(this)
        }
    }

    /*
    private fun postList() {
        instanceManager?.let { im -> list.postValue(im.plugins.map { it.plugin }) }
    }
    */

    override fun track(factoryAction: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
