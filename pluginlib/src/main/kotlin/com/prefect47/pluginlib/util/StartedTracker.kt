package com.prefect47.pluginlib.util

import java.util.concurrent.atomic.AtomicBoolean

class StartedTracker {
    val isStarted = AtomicBoolean(false)

    fun setStarted() {
        isStarted.set(true)
    }

    fun assertNotStarted() {
        if (isStarted.get()) throw IllegalStateException("Not allowed after library has been started")
    }

    fun assertStarted() {
        if (!isStarted.get()) throw IllegalStateException("Not allowed before library has been started")
    }
}
