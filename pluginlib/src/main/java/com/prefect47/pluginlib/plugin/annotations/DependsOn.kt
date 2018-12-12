package com.prefect47.pluginlib.plugin.annotations

import kotlin.annotation.Repeatable
import kotlin.annotation.Retention
import kotlin.annotation.AnnotationRetention
import kotlin.reflect.KClass

/**
 * Used to indicate that an interface in the plugin library needs another
 * interface to function properly. When this is added, it will be enforced
 * that all plugins that @Requires the annotated interface also @Requires
 * the specified class as well.
 */
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class DependsOn(val target: KClass<*>)
