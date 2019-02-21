package com.prefect47.pluginlib.impl.di.component

import android.content.Context
import com.prefect47.pluginlib.impl.*
import com.prefect47.pluginlib.impl.di.AppContext
import com.prefect47.pluginlib.impl.di.module.PluginLibraryFactoryModule
import com.prefect47.pluginlib.impl.di.module.PluginLibraryModule
import com.prefect47.pluginlib.plugin.PluginDependency
import com.prefect47.pluginlib.plugin.PluginLibraryControl
import dagger.Component
import javax.inject.Singleton
import dagger.BindsInstance



@Singleton
@Component(modules = [PluginLibraryModule::class, PluginLibraryFactoryModule::class])
interface PluginLibraryComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): PluginLibraryComponent
    }

    fun getPluginLibraryControl(): PluginLibraryControl
    fun getPluginPrefs(): PluginPrefs
    fun getPluginManager(): PluginManager
    fun getPluginInstanceManagerFactory(): PluginInstanceManager.Factory
    fun getPluginTrackerFactory(): PluginTrackerFactory

    //fun getDependencyProvider(): PluginDependency.DependencyProvider
    fun getPluginDependencyProvider(): PluginDependencyProvider
}
