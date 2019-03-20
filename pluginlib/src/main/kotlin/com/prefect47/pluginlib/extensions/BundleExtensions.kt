package com.prefect47.pluginlib.extensions

import android.os.Bundle

fun Bundle.copyStringTo(key: String, dest: Bundle) {
    dest.putString(key, getString(key))
}
