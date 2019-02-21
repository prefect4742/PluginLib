package com.prefect47.pluginlib.impl.di.module

import com.prefect47.pluginlib.impl.*
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PluginLibraryFactoryModule {
    @Singleton
    @Binds
    abstract fun bindPluginManagerFactory(factory: PluginManagerImpl.Factory): PluginManager.Factory

    @Singleton
    @Binds
    abstract fun bindPluginInstanceManagerFactory(factory: PluginInstanceManagerImpl.Factory): PluginInstanceManager.Factory
}
