package com.prefect47.pluginlib.impl.extensions

import android.view.View

val View.idName
    get() = resources.getResourceName(id).substringAfter("/")
