package com.prefect47.pluginlib.impl.di.module

import android.content.Context
//import com.prefect47.pluginlib.DependencyProviderImpl
import com.prefect47.pluginlib.impl.DiscoverablePrefs
import com.prefect47.pluginlib.impl.discoverables.factory.FactoryDiscoverableInfoImpl
import com.prefect47.pluginlib.impl.discoverables.plugin.PluginDiscoverableInfoImpl
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlib.impl.discoverables.plugin.PluginInfoFactory
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
