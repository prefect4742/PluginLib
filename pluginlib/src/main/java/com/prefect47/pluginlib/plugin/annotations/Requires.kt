package com.prefect47.pluginlib.plugin.annotations

import kotlin.annotation.Repeatable
import kotlin.annotation.Retention
import kotlin.annotation.AnnotationRetention
import kotlin.reflect.KClass

/**
 * Used to annotate which interfaces a given plugin depends on.
 *
 * At minimum all plugins should have at least one @Requires annotation
 * for the plugin interface that they are implementing. They will also
 * need an @Requires for each class that the plugin interface @DependsOn.
 */
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Requires(val target: KClass<*>, val version: Int)
