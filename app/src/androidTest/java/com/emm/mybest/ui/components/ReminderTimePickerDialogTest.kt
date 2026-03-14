package com.emm.mybest.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.emm.mybest.ui.theme.MyBestTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReminderTimePickerDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reminderTimePickerDialog_displaysInitialSelection() {
        composeTestRule.setContent {
            MyBestTheme {
                ReminderTimePickerDialog(
                    initialHour = 8,
                    initialMinute = 30,
                    onConfirm = { _, _ -> },
                    onDismiss = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Hora seleccionada 08:30")
            .assertIsDisplayed()
    }

    @Test
    fun reminderTimePickerDialog_updatesSelectionAndConfirms() {
        var confirmedHour = -1
        var confirmedMinute = -1

        composeTestRule.setContent {
            MyBestTheme {
                ReminderTimePickerDialog(
                    initialHour = 8,
                    initialMinute = 30,
                    onConfirm = { hour, minute ->
                        confirmedHour = hour
                        confirmedMinute = minute
                    },
                    onDismiss = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Hora 10", useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithContentDescription("Minutos 32", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Hora seleccionada 10:32")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Aceptar").performClick()

        assertEquals(10, confirmedHour)
        assertEquals(32, confirmedMinute)
    }

    @Test
    fun reminderTimePickerDialog_cancelTriggersDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            MyBestTheme {
                ReminderTimePickerDialog(
                    initialHour = 7,
                    initialMinute = 15,
                    onConfirm = { _, _ -> },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Cancelar").performClick()

        assertTrue(dismissed)
    }
}
