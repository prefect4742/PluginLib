package com.prefect47.pluginlibimpl.di.component

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.prefect47.pluginlibimpl.di.module.PluginLibraryBindings
import com.prefect47.pluginlibimpl.di.module.PluginLibraryModule
import com.prefect47.pluginlib.Manager
import com.prefect47.pluginlib.plugin.PluginDependency
import com.prefect47.pluginlib.Control
import com.prefect47.pluginlib.datastore.PluginPreferenceDataStoreManager
import dagger.Component
import javax.inject.Singleton
import dagger.BindsInstance

@Singleton
@Component(modules = [PluginLibraryModule::class, PluginLibraryBindings::class])
interface PluginLibraryComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun activity(activity: FragmentActivity): Builder

        fun build(): PluginLibraryComponent
    }

    fun inject(fragment: Fragment)

    fun getControl(): Control
    fun getManager(): Manager
    fun getDependencyProvider(): PluginDependency.DependencyProvider
    fun getDataStoreManager(): PluginPreferenceDataStoreManager
}
