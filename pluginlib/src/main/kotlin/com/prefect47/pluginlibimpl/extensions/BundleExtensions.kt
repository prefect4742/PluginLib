package com.prefect47.pluginlibimpl.extensions

import android.os.Bundle

fun Bundle.copyStringTo(key: String, dest: Bundle) {
    dest.putString(key, getString(key))
}
