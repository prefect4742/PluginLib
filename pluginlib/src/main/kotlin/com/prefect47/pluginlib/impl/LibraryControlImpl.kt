package com.prefect47.pluginlib.impl

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.prefect47.pluginlib.PluginLibProvidersImpl
import com.prefect47.pluginlib.impl.interfaces.FactoryManager
import com.prefect47.pluginlib.impl.interfaces.Manager
import com.prefect47.pluginlib.impl.interfaces.PluginInfoFactory
import com.prefect47.pluginlib.impl.viewmodel.PluginListViewModelFactory
import com.prefect47.pluginlib.impl.viewmodel.PluginListViewModelImpl
import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginSettingsEntrance
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.reflect.KClass

class LibraryControlImpl @Inject constructor(
    private val activity: FragmentActivity, private val managerLazy: Lazy<Manager>,
    private val pluginInfofactory: PluginInfoFactory, private val factoryManagerLazy: Lazy<FactoryManager>,
    override val preferenceDataStoreManager: PluginPreferenceDataStoreManager
): PluginLibraryControl {
    private val isStarted = AtomicBoolean(false)
    private val factoryActions = ArrayList<String>()
    private val listeners = ArrayList<PluginLibraryControl.StateListener>()
    override val staticProviders = ArrayList<PluginLibProviders>().apply {
        add(PluginLibProvidersImpl)
    }
    override val factories = ArrayList<PluginFactory>()

    //override val viewModel: PluginListViewModel
    //    get() = viewModelInner

    private val viewModelInner: PluginListViewModelImpl by lazy {
        ViewModelProviders.of(activity, PluginListViewModelFactory(manager, this))
            .get(PluginListViewModelImpl::class.java)
    }

    // We need to get() this double-lazily since we can't call it in the constructor since that would introduce a
    // circular call loop and exhaust the stack.
    private val manager: Manager by lazy { managerLazy.get() }
    private val factoryManager: FactoryManager by lazy { factoryManagerLazy.get() }

    override var settingsHandler: PluginSettingsEntrance.Callback? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    private fun assertNotStarted() {
        if (isStarted.get()) throw IllegalStateException("Not allowed after library has been started")
    }

    private fun assertStarted() {
        if (!isStarted.get()) throw IllegalStateException("Not allowed before library has been started")
    }

    override fun addClassFilter(filter: (String) -> Boolean) {
        assertNotStarted()
        manager.addClassFilter(filter)
    }

    override fun addStaticProviders(providers: PluginLibProviders) {
        staticProviders.add(providers)
    }

    override fun addFactory(factory: PluginFactory) {
        factories.add(factory)
    }

    override fun removeFactory(factory: PluginFactory) {
        factories.remove(factory)
    }

    /*
    override suspend fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>,
        action: String, allowMultiple : Boolean) {
        manager.addPluginListener(listener, cls, action, allowMultiple)
    }

    override fun removePluginListener(listener: PluginListener<*>) {
        manager.removePluginListener(listener)
    }
    */

    override fun track(cls: KClass<out Plugin>) {
        assertNotStarted()
        viewModelInner.track(cls)
    }

    override fun track(factoryAction: String) {
        assertNotStarted()
        factoryManager.track(factoryAction)
    }

    override fun addStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.remove(listener)
    }

    override suspend fun start() {
        assertNotStarted()
        debug("PluginLib starting")
        factoryManager.start()
        viewModelInner.start()
        isStarted.set(true)
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

    override suspend fun startPlugin(plugin: Plugin) {
        assertStarted()
        withContext(Dispatchers.Main) {
            plugin.onStart()
        }
    }

    override suspend fun stopPlugin(plugin: Plugin) {
        assertStarted()
        withContext(Dispatchers.Main) {
            plugin.onStop()
        }
    }

    override fun getPluginList(pluginClass: KClass<out Plugin>): List<PluginInfo<out Plugin>>? {
        assertStarted()
        return viewModelInner.list[pluginClass]?.plugins?.value?.map { pluginInfofactory.create(it) }
    }

    override fun getFlags(pluginClassName: String): EnumSet<Discoverable.Flag>? {
        assertStarted()
        return manager.discoverableClassFlagsMap[pluginClassName]
    }

    override fun getPlugin(className: String): PluginInfo<out Plugin>? {
        assertStarted()
        viewModelInner.list.values.forEach { model ->
            model.plugins.value?.find { it.component.className == className }?.let {
                return pluginInfofactory.create(it)
            }
        }
        return null
    }
}
