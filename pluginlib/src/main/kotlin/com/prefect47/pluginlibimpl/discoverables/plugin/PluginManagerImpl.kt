package com.prefect47.pluginlibimpl.discoverables.plugin

import android.util.Log
import com.prefect47.pluginlib.discoverables.plugin.PluginDiscoverableInfo.Listener
import com.prefect47.pluginlib.DiscoverableManager
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlib.discoverables.plugin.Plugin
import com.prefect47.pluginlib.Control
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import com.prefect47.pluginlib.discoverables.plugin.PluginDiscoverableInfo
import com.prefect47.pluginlib.discoverables.plugin.PluginInfo
import com.prefect47.pluginlib.discoverables.plugin.PluginManager
import com.prefect47.pluginlib.discoverables.plugin.PluginManager.PluginInfoHook
import com.prefect47.pluginlib.discoverables.plugin.PluginManager.PluginList
import com.prefect47.pluginlibimpl.util.StartedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.reflect.KClass

class PluginManagerImpl @Inject constructor(
    private val manager: Manager, private val control: Control,
    private val discoverableInfoFactory: PluginDiscoverableInfo.Factory,
    private val factoryManager: FactoryManager
): PluginManager {
    companion object {
        const val TAG = "PluginManager"
    }

    override val hooks = ArrayList<PluginInfoHook>()

    override val list = HashMap<KClass<out Plugin>, PluginList<out Plugin>>()
    private val started = StartedTracker()

    inner class PluginListImpl<T: Plugin>(val cls: KClass<T>) : PluginList<T>, Listener {
        override val plugins: List<PluginDiscoverableInfo>
            get() = innerManager.discoverables

        override val discoverableManager: DiscoverableManager<T, PluginDiscoverableInfo>
            get() = innerManager

        private lateinit var innerManager: DiscoverableManager<T, PluginDiscoverableInfo>

        override fun onStartDiscovering() {
            control.debug("Starting tracking plugins: ${cls.qualifiedName}")
        }

        override fun onDoneDiscovering() {
            control.debug("Started tracking plugins: ${cls.qualifiedName}")
        }

        override fun onDiscovered(info: PluginDiscoverableInfo) {
            if (control.debugEnabled) Log.d(TAG, "Found plugin ${info.component.className}")
        }

        override fun onRemoved(info: PluginDiscoverableInfo) {
            if (control.debugEnabled) Log.d(TAG, "Plugin ${info.component.className} was removed")
        }

        suspend fun start() {
            innerManager = manager.addListener(this, cls, allowMultiple = true,
                discoverableInfoFactory = discoverableInfoFactory)
        }

        fun stop() {
            manager.removeListener(this)
        }
    }

    override fun track(cls: KClass<out Plugin>) {
        started.assertNotStarted()
        list[cls] = PluginListImpl(cls)
    }

    override fun <T : Plugin> getList(pluginClass: KClass<T>): List<PluginInfo<T>>? {
        started.assertStarted()
        return list[pluginClass]?.plugins?.map { it.makePluginInfo<T>() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getList(pluginClassName: String): List<PluginInfo<out Plugin>>? {
        started.assertStarted()
        return getList(factoryManager.findClass(pluginClassName) as KClass<out Plugin>)
    }

    override fun <T: Plugin> get(cls: KClass<T>): PluginInfo<T>? {
        started.assertStarted()
        list.values.forEach { list ->
            list.plugins.find { it.cls == cls }?.let {
                return it.makePluginInfo()
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(clsName: String): PluginInfo<out Plugin>? {
        started.assertStarted()
        return get(factoryManager.findClass(clsName) as KClass<out Plugin>)
    }

    override fun <T : Plugin> getFlags(pluginClass: KClass<T>): Set<String> {
        started.assertStarted()
        return list[pluginClass]?.discoverableManager?.flags ?: emptySet()
    }

    override fun getFlags(pluginClassName: String): Set<String> {
        started.assertStarted()
        return list.values.find {
            it.discoverableManager.cls.qualifiedName == pluginClassName
        }?.discoverableManager?.flags ?: emptySet()
    }

    override suspend fun start() {
        if (control.debugEnabled) Log.d(TAG, "Starting")
        withContext(Dispatchers.Default) {
            for ((_, model) in list) {
                launch {
                    (model as PluginListImpl).start() }
            }
        }
        started.setStarted()
        if (control.debugEnabled) Log.d(TAG, "Started")
    }

    fun stop() {
        for ((_, model) in list) {
            (model as PluginListImpl).stop()
        }
    }
}
