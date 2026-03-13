package com.emm.mybest.features.timeline.presentation

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineNavigationComponentsTest {

    @Test
    fun `resolveDateJumpTarget returns previous and next dates when available`() {
        val dates = listOf(
            LocalDate(2026, 3, 10),
            LocalDate(2026, 3, 11),
            LocalDate(2026, 3, 12),
        )

        val previous = resolveDateJumpTarget(
            dates = dates,
            selectedDate = LocalDate(2026, 3, 11),
            direction = DateJumpDirection.PREVIOUS,
        )
        val next = resolveDateJumpTarget(
            dates = dates,
            selectedDate = LocalDate(2026, 3, 11),
            direction = DateJumpDirection.NEXT,
        )

        assertEquals(LocalDate(2026, 3, 10), previous)
        assertEquals(LocalDate(2026, 3, 12), next)
    }

    @Test
    fun `resolveDateJumpTarget returns null on range limits`() {
        val dates = listOf(
            LocalDate(2026, 3, 10),
            LocalDate(2026, 3, 11),
        )

        val previousAtStart = resolveDateJumpTarget(
            dates = dates,
            selectedDate = LocalDate(2026, 3, 10),
            direction = DateJumpDirection.PREVIOUS,
        )
        val nextAtEnd = resolveDateJumpTarget(
            dates = dates,
            selectedDate = LocalDate(2026, 3, 11),
            direction = DateJumpDirection.NEXT,
        )

        assertEquals(null, previousAtStart)
        assertEquals(null, nextAtEnd)
    }

    @Test
    fun `resolveDateJumpTarget falls back to first date when selected is missing`() {
        val dates = listOf(
            LocalDate(2026, 3, 10),
            LocalDate(2026, 3, 11),
        )

        val target = resolveDateJumpTarget(
            dates = dates,
            selectedDate = LocalDate(2026, 3, 20),
            direction = DateJumpDirection.NEXT,
        )

        assertEquals(LocalDate(2026, 3, 10), target)
    }
}
