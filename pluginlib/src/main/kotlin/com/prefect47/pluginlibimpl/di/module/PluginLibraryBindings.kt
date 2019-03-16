package com.prefect47.pluginlibimpl.di.module

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.prefect47.pluginlib.Control
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager
import com.prefect47.pluginlibimpl.datastore.PreferenceDataStoreManagerImpl
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverableInfo
import com.prefect47.pluginlibimpl.discoverables.factory.FactoryDiscoverableInfoImpl
import com.prefect47.pluginlibimpl.discoverables.factory.FactoryManagerImpl
import com.prefect47.pluginlib.discoverables.plugin.*
import com.prefect47.pluginlibimpl.DependencyProviderImpl
import com.prefect47.pluginlibimpl.DiscoverableManagerImpl
import com.prefect47.pluginlibimpl.ControlImpl
import com.prefect47.pluginlibimpl.ManagerImpl
import com.prefect47.pluginlib.discoverables.factory.FactoryManager
import com.prefect47.pluginlib.DiscoverableManager
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlibimpl.discoverables.plugin.*
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
    abstract fun bindControl(control: ControlImpl): Control

    @Singleton
    @Binds
    abstract fun bindManagerFactory(factory: ManagerImpl.Factory): Manager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginInfoFactory(factory: PluginInfoImpl.Factory): PluginInfoFactory

    /*
    @Singleton
    @Binds
    abstract fun <T: Plugin> bindInstanceInfoFactory(factory: PluginDiscoverableInfoImpl.Factory<T>): DiscoverableInfo.Factory<T>
    */

    @Singleton
    @Binds
    abstract fun bindPluginDiscoverableInfoFactory(factory: PluginDiscoverableInfoImpl.Factory): PluginDiscoverableInfo.Factory

    @Singleton
    @Binds
    abstract fun bindFactoryDiscoverableInfoFactory(factory: FactoryDiscoverableInfoImpl.Factory): FactoryDiscoverableInfo.Factory

    @Singleton
    @Binds
    abstract fun bindFactoryManager(manager: FactoryManagerImpl): FactoryManager

    @Singleton
    @Binds
    abstract fun bindPluginManager(manager: PluginManagerImpl): PluginManager

    @Singleton
    @Binds
    abstract fun bindInstanceManagerFactory(factory: DiscoverableManagerImpl.Factory): DiscoverableManager.Factory

    /*
    @Singleton
    @Binds
    abstract fun bindPluginListViewModel(model: PluginListViewModelImpl): PluginListViewModel
    */

    @Singleton
    @Binds
    abstract fun bindDependencyProvider(provider: DependencyProviderImpl): PluginDependency.DependencyProvider

    @Singleton
    @Binds
    abstract fun bindPrefsManager(manager: PreferenceDataStoreManagerImpl): PluginPreferenceDataStoreManager
}
