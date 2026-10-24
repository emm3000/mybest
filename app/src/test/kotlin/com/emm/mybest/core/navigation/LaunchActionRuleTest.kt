package com.emm.mybest.core.navigation

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
}
