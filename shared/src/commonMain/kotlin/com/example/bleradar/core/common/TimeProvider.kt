package com.example.bleradar.core.common

import kotlin.time.Clock

fun interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}