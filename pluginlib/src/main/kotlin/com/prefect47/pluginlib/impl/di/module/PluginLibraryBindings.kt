package com.prefect47.pluginlib.impl.di.module

import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.plugin.PluginDependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import com.prefect47.pluginlib.plugin.PluginPreferenceDataStoreManager
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PluginLibraryBindings {
    @Singleton
    @Binds
    abstract fun bindControl(control: LibraryControlImpl): PluginLibraryControl

    @Singleton
    @Binds
    abstract fun bindManagerFactory(factory: ManagerImpl.Factory): Manager.Factory

    @Singleton
    @Binds
    abstract fun bindInstanceManagerFactory(factory: InstanceManagerImpl.Factory): InstanceManager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginTrackerFactory(factory: TrackerImpl.Factory): TrackerFactory

    @Singleton
    @Binds
    abstract fun bindDependencyProvider(provider: DependencyProviderImpl): PluginDependency.DependencyProvider

    @Singleton
    @Binds
    abstract fun bindPrefsManager(manager: PreferenceDataStoreManagerImpl): PluginPreferenceDataStoreManager
}
