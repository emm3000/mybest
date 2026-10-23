package com.emm.mybest.testing

import kotlin.time.Clock
import kotlin.time.Instant

class MutableClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}
