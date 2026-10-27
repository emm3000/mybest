package com.emm.mybest.core.navigation

import com.emm.mybest.domain.usecase.home.HomeDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val MAIN_ACTION = "android.intent.action.MAIN"

class LaunchActionRuleTest {

    @Test
    fun `first creation runs the intent action`() {
        val pending = LaunchActionRule.resolve(null, ACTION_DEBTS, isFromHistory = false)

        assertEquals(ACTION_DEBTS, pending)
    }

    @Test
    fun `first creation from history runs nothing`() {
        val pending = LaunchActionRule.resolve(null, ACTION_DEBTS, isFromHistory = true)

        assertNull(pending)
    }

    @Test
    fun `first creation keeps the main action`() {
        val pending = LaunchActionRule.resolve(null, MAIN_ACTION, isFromHistory = false)

        assertEquals(MAIN_ACTION, pending)
    }

    @Test
    fun `recreation after the action was consumed runs nothing`() {
        val saved = LaunchActionRule.Saved(pendingAction = null)

        val pending = LaunchActionRule.resolve(saved, ACTION_DEBTS, isFromHistory = false)

        assertNull(pending)
    }

    @Test
    fun `recreation keeps the saved action that was not consumed`() {
        val saved = LaunchActionRule.Saved(pendingAction = ACTION_CHECK_IN)

        val pending = LaunchActionRule.resolve(saved, ACTION_CHECK_IN, isFromHistory = false)

        assertEquals(ACTION_CHECK_IN, pending)
    }

    @Test
    fun `recreation prefers the saved action over the intent action`() {
        val saved = LaunchActionRule.Saved(pendingAction = ACTION_CHECK_IN)

        val pending = LaunchActionRule.resolve(saved, ACTION_DEBTS, isFromHistory = false)

        assertEquals(ACTION_CHECK_IN, pending)
    }

    @Test
    fun `an action for the screen Home shows on screen stays on Home`() {
        val screens = listOf(
            LaunchActionRule.screenFor(ACTION_CHECK_IN, HomeDestination.CHECK_IN, isHomeVisible = true),
            LaunchActionRule.screenFor(ACTION_PRE_OUTING, HomeDestination.PRE_OUTING, isHomeVisible = true),
            LaunchActionRule.screenFor(ACTION_WEEKLY_SUMMARY, HomeDestination.WEEKLY_SUMMARY, isHomeVisible = true),
        )

        assertEquals(listOf(Screen.Home, Screen.Home, Screen.Home), screens)
    }

    @Test
    fun `check-in action pushes the check-in when Home shows the daily checks`() {
        val screen = LaunchActionRule.screenFor(ACTION_CHECK_IN, HomeDestination.DAILY_CHECKS, isHomeVisible = true)

        assertEquals(Screen.CheckIn, screen)
    }

    @Test
    fun `check-in action pushes the check-in when Home shows another embedded screen`() {
        val screen = LaunchActionRule.screenFor(ACTION_CHECK_IN, HomeDestination.PRE_OUTING, isHomeVisible = true)

        assertEquals(Screen.CheckIn, screen)
    }

    @Test
    fun `check-in action pushes the check-in when Home's destination is unknown`() {
        val screen = LaunchActionRule.screenFor(ACTION_CHECK_IN, homeDestination = null, isHomeVisible = true)

        assertEquals(Screen.CheckIn, screen)
    }

    @Test
    fun `check-in action pushes the check-in when Home is not on screen`() {
        val screen = LaunchActionRule.screenFor(ACTION_CHECK_IN, HomeDestination.CHECK_IN, isHomeVisible = false)

        assertEquals(Screen.CheckIn, screen)
    }

    @Test
    fun `pre-outing action pushes the pre-outing when Home shows the daily checks`() {
        val screen = LaunchActionRule.screenFor(ACTION_PRE_OUTING, HomeDestination.DAILY_CHECKS, isHomeVisible = true)

        assertEquals(Screen.PreOuting, screen)
    }

    @Test
    fun `weekly summary action opens the summary when Home shows the daily checks`() {
        val screen = LaunchActionRule.screenFor(
            ACTION_WEEKLY_SUMMARY,
            HomeDestination.DAILY_CHECKS,
            isHomeVisible = true,
        )

        assertEquals(Screen.WeeklySummary, screen)
    }

    @Test
    fun `actions Home never embeds open their screen`() {
        val screens = listOf(ACTION_ADD_WEIGHT, ACTION_ADD_PHOTO, ACTION_DEBTS)
            .map { LaunchActionRule.screenFor(it, HomeDestination.CHECK_IN, isHomeVisible = true) }

        assertEquals(listOf(Screen.AddWeight, Screen.Photos, Screen.Debts), screens)
    }

    @Test
    fun `an unknown action opens nothing`() {
        val screen = LaunchActionRule.screenFor(MAIN_ACTION, HomeDestination.CHECK_IN, isHomeVisible = true)

        assertNull(screen)
    }
}
