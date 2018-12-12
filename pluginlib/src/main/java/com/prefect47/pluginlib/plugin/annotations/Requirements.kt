package com.prefect47.pluginlib.plugin.annotations

import kotlin.annotation.Retention
import kotlin.annotation.AnnotationRetention

/**
 * Used for repeated @Requires internally, not for plugin
 * use.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Requirements(vararg val value: Requires)
