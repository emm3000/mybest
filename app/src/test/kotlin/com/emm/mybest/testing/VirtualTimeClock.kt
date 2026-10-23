package com.emm.mybest.testing

import kotlinx.coroutines.test.TestScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class VirtualTimeClock(private val start: Instant, private val scope: TestScope) : Clock {
    override fun now(): Instant = start + scope.testScheduler.currentTime.milliseconds
}
