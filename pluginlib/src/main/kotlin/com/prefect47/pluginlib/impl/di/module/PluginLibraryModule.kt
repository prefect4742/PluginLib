package com.prefect47.pluginlib.impl.di.module

import android.content.Context
import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.impl.interfaces.Manager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PluginLibraryModule {
    @Singleton
    @Provides
    fun providePluginPrefs(context: Context) = PluginPrefs(context)

    @Singleton
    @Provides
    fun provideManager(factory: Manager.Factory) = factory.create()

    @Singleton
    @Provides
    fun providePluginDependencyProvider(manager: Manager) = DependencyProviderImpl(manager)
}
