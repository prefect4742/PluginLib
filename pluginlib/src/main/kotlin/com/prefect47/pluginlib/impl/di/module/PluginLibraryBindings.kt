package com.prefect47.pluginlib.impl.di.module

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.impl.instances.PluginInstanceInfoImpl
import com.prefect47.pluginlib.impl.interfaces.*
import com.prefect47.pluginlib.impl.viewmodel.PluginListViewModelImpl
import com.prefect47.pluginlib.plugin.PluginDependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager
import com.prefect47.pluginlib.viewmodel.PluginListViewModel
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PluginLibraryBindings {
    @Singleton
    @Binds
    abstract fun bindContext(activity: FragmentActivity): Context

    @Singleton
    @Binds
    abstract fun bindControl(control: LibraryControlImpl): PluginLibraryControl

    @Singleton
    @Binds
    abstract fun bindManagerFactory(factory: ManagerImpl.Factory): Manager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginInfoFactory(factory: PluginInfoImpl.Factory): PluginInfoFactory

    @Singleton
    @Binds
    abstract fun bindInstanceInfoFactory(factory: PluginInstanceInfoImpl.Factory): InstanceInfo.Factory

    @Singleton
    @Binds
    abstract fun bindFactoryManager(factory: FactoryManagerImpl): FactoryManager

    @Singleton
    @Binds
    abstract fun bindInstanceManagerFactory(factory: InstanceManagerImpl.Factory): InstanceManager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginListViewModel(model: PluginListViewModelImpl): PluginListViewModel

    @Singleton
    @Binds
    abstract fun bindDependencyProvider(provider: DependencyProviderImpl): PluginDependency.DependencyProvider

    @Singleton
    @Binds
    abstract fun bindPrefsManager(manager: PreferenceDataStoreManagerImpl): PluginPreferenceDataStoreManager
}
