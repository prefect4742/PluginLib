package com.prefect47.pluginlib.di.module

import android.content.Context
//import com.prefect47.pluginlib.DependencyProviderImpl
import com.prefect47.pluginlib.DiscoverablePrefs
import com.prefect47.pluginlib.discoverables.factory.FactoryDiscoverableInfoImpl
import com.prefect47.pluginlib.discoverables.plugin.PluginDiscoverableInfoImpl
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlib.discoverables.plugin.PluginInfoFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PluginLibraryModule {
    @Singleton
    @Provides
    fun providePluginPrefs(context: Context) = DiscoverablePrefs(context)

    @Singleton
    @Provides
    fun provideManager(factory: Manager.Factory) = factory.create()

    /*
    @Singleton
    @Provides
    fun providePluginDependencyProvider(manager: Manager) =
        DependencyProviderImpl(manager)
    */

    @Singleton
    @Provides
    fun providePluginDiscoverableInfoFactory(pluginInfoFactory: PluginInfoFactory) =
        PluginDiscoverableInfoImpl.Factory(pluginInfoFactory)

    @Singleton
    @Provides
    fun provideFactoryDiscoverableInfoFactory() = FactoryDiscoverableInfoImpl.Factory()
}
