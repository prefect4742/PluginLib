package com.prefect47.pluginlib.impl.di.module

import android.content.Context
import com.prefect47.pluginlib.impl.PluginPrefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PluginLibraryModule {
    @Singleton
    @Provides
    fun providePluginPrefs(context: Context) = PluginPrefs(context)
}
