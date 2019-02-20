package com.prefect47.pluginlib.impl.di.component

import android.content.Context
import com.prefect47.pluginlib.impl.PluginInstanceManager
import com.prefect47.pluginlib.impl.PluginManager
import com.prefect47.pluginlib.impl.PluginManagerImpl
import com.prefect47.pluginlib.impl.PluginPrefs
import com.prefect47.pluginlib.impl.di.AppContext
import com.prefect47.pluginlib.impl.di.module.PluginLibraryModule
import dagger.Component
import javax.inject.Singleton
import dagger.BindsInstance



@Singleton
@Component(modules = [PluginLibraryModule::class])
interface PluginLibraryComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): PluginLibraryComponent
    }

    //fun inject(target: PluginInstanceManager<*>)
    fun inject(target: PluginManagerImpl)

    fun getPluginPrefs(): PluginPrefs
}
