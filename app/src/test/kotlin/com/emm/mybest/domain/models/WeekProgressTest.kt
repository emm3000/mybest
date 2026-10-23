package com.emm.mybest.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test

class WeekProgressTest {

    @Test
    fun `exceededBy is the days over the target`() {
        assertEquals(1, WeekProgress(count = 3, target = 2).exceededBy)
    }

    @Test
    fun `exceededBy is zero at or under the target`() {
        assertEquals(0, WeekProgress(count = 2, target = 2).exceededBy)
        assertEquals(0, WeekProgress(count = 1, target = 2).exceededBy)
    }
}
