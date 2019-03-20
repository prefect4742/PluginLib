package com.prefect47.pluginlib.extensions

import androidx.preference.Preference
import androidx.preference.PreferenceGroup

fun PreferenceGroup.asSequence(): Sequence<Preference> = object : Sequence<Preference> {
    override fun iterator(): Iterator<Preference> = object : Iterator<Preference> {
        private var position: Int = 0
        override fun hasNext() = position < preferenceCount
        override fun next() = getPreference(position++)
    }
}

val PreferenceGroup.preferences: List<Preference>
    get() = asSequence().toList()

val PreferenceGroup.preferencesRecursive: List<Preference>
    get() = preferences.flatMap {
        when (it) {
            is PreferenceGroup -> it.preferencesRecursive
            else -> listOf(it)
        }
    }
