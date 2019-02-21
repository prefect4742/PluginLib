package com.prefect47.pluginlib.impl.di.module

import android.content.Context
import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PluginLibraryModule {
    @Singleton
    @Provides
    fun providsPluginLibraryControl(): PluginLibraryControl = PluginLibraryControlImpl

    @Singleton
    @Provides
    fun providePluginPrefs(context: Context) = PluginPrefs(context)

    @Singleton
    @Provides
    fun providePluginManager(context: Context) = PluginManagerImpl.create(context)

    @Singleton
    @Provides
    fun providePluginInstanceManagerFactory(context: Context, manager: PluginManager, control: PluginLibraryControl,
        pluginPrefs: PluginPrefs) = PluginInstanceManagerImpl.Factory(context, manager, control, pluginPrefs)
}
