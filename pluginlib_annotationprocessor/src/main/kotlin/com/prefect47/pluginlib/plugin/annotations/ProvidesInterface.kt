package com.prefect47.pluginlib.plugin.annotations

import kotlin.annotation.Retention
import kotlin.annotation.AnnotationRetention

/**
 * Should be added to all interfaces in plugin lib to specify their
 * current version and optionally their action to implement the plugin.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProvidesInterface(val version: Int, val action: String = "")
