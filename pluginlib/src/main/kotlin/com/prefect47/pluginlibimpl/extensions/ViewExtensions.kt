package com.prefect47.pluginlibimpl.extensions

import android.view.View

val View.idName
    get() = resources.getResourceName(id).substringAfter("/")
