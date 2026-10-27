package com.emm.mybest.core.navigation

import com.emm.mybest.domain.usecase.home.HomeDestination
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeVisibleDestinationTest {

    private val holder = HomeVisibleDestination()
    private val oldHome = Any()
    private val newHome = Any()

    @Test
    fun `the publishing Home clears its destination`() {
        holder.publish(oldHome, HomeDestination.CHECK_IN)

        holder.clear(oldHome)

        assertNull(holder.destination.value)
    }

    @Test
    fun `an older Home going away keeps what a newer Home published`() {
        holder.publish(oldHome, HomeDestination.DAILY_CHECKS)
        holder.publish(newHome, HomeDestination.CHECK_IN)

        holder.clear(oldHome)

        assertEquals(HomeDestination.CHECK_IN, holder.destination.value)
    }

    @Test
    fun `waiting for a loaded Home returns its destination`() = runTest {
        holder.publish(newHome, HomeDestination.PRE_OUTING)

        assertEquals(HomeDestination.PRE_OUTING, holder.awaitLoaded())
    }

    @Test
    fun `waiting for a Home that never loads gives up`() = runTest {
        assertNull(holder.awaitLoaded())
    }
}
