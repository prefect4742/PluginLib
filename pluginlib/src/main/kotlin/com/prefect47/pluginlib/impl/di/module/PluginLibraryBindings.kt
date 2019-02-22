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
    abstract fun bindControl(control: PluginLibraryControlImpl): PluginLibraryControl

    @Singleton
    @Binds
    abstract fun bindManagerFactory(factory: PluginManagerImpl.Factory): PluginManager.Factory

    @Singleton
    @Binds
    abstract fun bindInstanceManagerFactory(factory: PluginInstanceManagerImpl.Factory): PluginInstanceManager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginTrackerFactory(factory: PluginTrackerImpl.Factory): PluginTrackerFactory

    @Singleton
    @Binds
    abstract fun bindDependencyProvider(provider: PluginDependencyProvider): PluginDependency.DependencyProvider

    @Singleton
    @Binds
    abstract fun bindPrefsManager(manager: PluginPreferenceDataStoreManagerImpl): PluginPreferenceDataStoreManager
}
