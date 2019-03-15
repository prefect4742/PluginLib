package com.prefect47.pluginlibimpl.extensions

import android.view.View
import android.view.ViewGroup

inline fun ViewGroup.forEach(action: (View) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}

fun ViewGroup.asSequence(): Sequence<View> = object : Sequence<View> {
    override fun iterator(): Iterator<View> = object : Iterator<View> {
        private var position: Int = 0
        override fun hasNext() = position < childCount
        override fun next() = getChildAt(position++)
    }
}

val ViewGroup.views: List<View>
    get() = asSequence().toList()

val ViewGroup.viewsRecursive: List<View>
    get() = views.flatMap {
        when (it) {
            is ViewGroup -> it.viewsRecursive
            else -> listOf(it)
        }
    }
