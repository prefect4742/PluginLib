package com.prefect47.pluginlib

//import com.prefect47.pluginlib.PluginLibProvidersImpl
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import com.prefect47.pluginlib.discoverables.plugin.PluginManager
import com.prefect47.pluginlib.discoverables.plugin.PluginSettingsEntrance
import com.prefect47.pluginlib.util.StartedTracker
import dagger.Lazy
import java.util.*
import javax.inject.Inject

class ControlImpl @Inject constructor(
    private val managerLazy: Lazy<Manager>,
    private val factoryManagerLazy: Lazy<FactoryManager>,
    private val pluginManagerLazy: Lazy<PluginManager>,
    override val preferenceDataStoreManager: PluginPreferenceDataStoreManager
): Control {
    private val started = StartedTracker()

    private val listeners = ArrayList<Control.StateListener>()
    override val staticProviders = ArrayList<Providers>() /*.apply {
        add(PluginLibProvidersImpl)
    }*/

    // We need to get() this double-lazily since we can't call it in the constructor since that would introduce a
    // circular call loop and exhaust the stack.
    override val manager: Manager by lazy { managerLazy.get() }
    override val factoryManager: FactoryManager by lazy { factoryManagerLazy.get() }
    override val pluginManager: PluginManager by lazy { pluginManagerLazy.get() }

    override var settingsHandler: PluginSettingsEntrance.Callback? = null
    override var permissionName: String = Control.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = Control.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override fun addClassFilter(filter: (String) -> Boolean) {
        started.assertNotStarted()
        manager.addClassFilter(filter)
    }

    override fun addStaticProviders(providers: Providers) {
        staticProviders.add(providers)
    }

    override fun addStateListener(listener: Control.StateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: Control.StateListener) {
        listeners.remove(listener)
    }

    override suspend fun start() {
        started.assertNotStarted()
        debug("PluginLib starting")
        factoryManager.start()
        pluginManager.start()
        started.setStarted()
        debug("PluginLib started")
        listeners.forEach { it.onStarted() }
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*
    override suspend fun startPlugin(plugin: Plugin) {
        started.assertStarted()
        withContext(Dispatchers.Main) {
            plugin.onStart()
        }
    }

    override suspend fun stopPlugin(plugin: Plugin) {
        started.assertStarted()
        withContext(Dispatchers.Main) {
            plugin.onStop()
        }
    }
    */
}
