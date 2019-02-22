package com.prefect47.pluginlib.impl.di.module

import android.content.Context
import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.plugin.PluginDependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import dagger.Lazy
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PluginLibraryModule {
    @Singleton
    @Provides
    fun providePluginLibraryControl(): PluginLibraryControl = PluginLibraryControlImpl

    @Singleton
    @Provides
    fun providePluginPrefs(context: Context) = PluginPrefs(context)

    @Singleton
    @Provides
    fun providePluginManager(factory: PluginManager.Factory) = factory.create()

    @Singleton
    @Provides
    fun providePluginManagerFactory(context: Context, control: PluginLibraryControl, pluginPrefs: PluginPrefs,
            factoryLazy: Lazy<PluginInstanceManager.Factory>) =
        PluginManagerImpl.Factory(context, control, pluginPrefs, factoryLazy)

    @Singleton
    @Provides
    fun providePluginInstanceManagerFactory(context: Context, manager: PluginManager, control: PluginLibraryControl,
        pluginPrefs: PluginPrefs) = PluginInstanceManagerImpl.Factory(context, manager, control, pluginPrefs)

    @Singleton
    @Provides
    fun providePluginTrackerFactory(manager: PluginManager, control: PluginLibraryControl,
        dependencyProvider: PluginDependencyProvider) = PluginTrackerImpl.Factory(manager, control, dependencyProvider)

    @Singleton
    @Provides
    fun providePluginDependencyProvider(manager: PluginManager) = PluginDependencyProvider(manager)
}
