package com.prefect47.pluginlib.impl

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.prefect47.pluginlib.PluginLibProvidersImpl
import com.prefect47.pluginlib.impl.viewmodel.PluginListViewModelFactory
import com.prefect47.pluginlib.impl.viewmodel.PluginListViewModelImpl
import com.prefect47.pluginlib.plugin.*
import com.prefect47.pluginlib.ui.preference.PluginListCategory
import com.prefect47.pluginlib.viewmodel.PluginListViewModel
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

class LibraryControlImpl @Inject constructor(
    private val activity: FragmentActivity, private val managerLazy: Lazy<Manager>,
    override val preferenceDataStoreManager: PluginPreferenceDataStoreManager
): PluginLibraryControl {
    private val listeners = ArrayList<PluginLibraryControl.StateListener>()
    override val staticProviders = ArrayList<PluginLibProviders>().apply {
        add(PluginLibProvidersImpl)
    }
    override val factories = ArrayList<PluginFactory>()

    override val viewModel: PluginListViewModel
        get() = viewModelInner

    private val viewModelInner: PluginListViewModelImpl by lazy {
        ViewModelProviders.of(activity, PluginListViewModelFactory(manager, this))
            .get(PluginListViewModelImpl::class.java)
    }

    // We need to get() this double-lazily since we can't call it in the constructor since that would introduce a
    // circular call loop and exhaust the stack.
    private val manager: Manager by lazy { managerLazy.get() }

    override var settingsHandler: PluginListCategory.SettingsHandler? = null
    override var permissionName: String = PluginLibraryControl.DEFAULT_PERMISSIONNAME
    override var debugEnabled: Boolean = false
    override var debugTag: String = PluginLibraryControl.DEFAULT_DEBUGTAG
    override var notificationChannel: String? = null
    override var notificationIconResId: Int = 0

    override fun addClassFilter(filter: (String) -> Boolean) {
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

    override suspend fun <T: Plugin> addPluginListener(listener: PluginListener<T>, cls: KClass<T>,
        action: String, allowMultiple : Boolean) {
        manager.addPluginListener(listener, cls, action, allowMultiple)
    }

    override fun removePluginListener(listener: PluginListener<*>) {
        manager.removePluginListener(listener)
    }

    override fun track(cls: KClass<out Plugin>) {
        viewModelInner.track(cls)
    }

    override fun addStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: PluginLibraryControl.StateListener) {
        listeners.remove(listener)
    }

    override suspend fun start() {
        debug("PluginLib starting")
        viewModelInner.start()
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
        withContext(Dispatchers.Main) {
            plugin.onStart()
        }
    }

    override suspend fun stopPlugin(plugin: Plugin) {
        withContext(Dispatchers.Main) {
            plugin.onStop()
        }
    }

    override fun getPluginList(cls: KClass<out Plugin>): List<Plugin>? = viewModelInner.list[cls]?.plugins?.value

    override fun getPluginList(pluginClassName: String): List<Plugin>? =
        getPluginList(Class.forName(pluginClassName).kotlin as KClass<out Plugin>)

    override fun getFlags(pluginClassName: String): EnumSet<Plugin.Flag>? =
        manager.pluginClassFlagsMap[pluginClassName]

    override fun getPlugin(className: String): Plugin? {
        viewModelInner.list.values.forEach { model ->
            model.plugins.value?.find { plugin -> plugin::class.qualifiedName == className }?.let { return it }
        }
        return null
    }
}
