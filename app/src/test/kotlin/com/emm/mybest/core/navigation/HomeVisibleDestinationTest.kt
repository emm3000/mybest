package com.emm.mybest.core.navigation

import com.emm.mybest.domain.usecase.home.HomeDestination
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class HomeVisibleDestinationTest {

    private val holder = HomeVisibleDestination()
    private val oldHome = Any()
    private val newHome = Any()
    private var isHomeVisible = true

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
    fun `a Home that loads during the wait returns its destination`() = runTest {
        val wait = async { holder.awaitLoaded() }
        advanceTimeBy(1.seconds)

        holder.publish(newHome, HomeDestination.CHECK_IN)

        assertEquals(HomeDestination.CHECK_IN, wait.await())
    }

    @Test
    fun `a launch action stays on Home when Home loads with its screen while on screen`() = runTest {
        val screen = async { holder.screenForLaunchAction(ACTION_CHECK_IN) { isHomeVisible } }
        advanceTimeBy(1.seconds)

        holder.publish(newHome, HomeDestination.CHECK_IN)

        assertEquals(Screen.Home, screen.await())
    }

    @Test
    fun `a launch action pushes its screen when the user leaves Home during the wait`() = runTest {
        val screen = async { holder.screenForLaunchAction(ACTION_CHECK_IN) { isHomeVisible } }
        advanceTimeBy(1.seconds)

        isHomeVisible = false
        holder.publish(newHome, HomeDestination.CHECK_IN)

        assertEquals(Screen.CheckIn, screen.await())
    }

    @Test
    fun `waiting for a Home that never loads gives up`() = runTest {
        assertNull(holder.awaitLoaded())
    }
}
