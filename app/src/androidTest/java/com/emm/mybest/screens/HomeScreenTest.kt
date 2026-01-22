package com.emm.mybest.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.HomeState
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysTitleAndSummary() {
        val state = HomeState(
            lastWeight = 70.0f,
            habitsCompletedToday = 2,
            totalPhotos = 5,
            isLoading = false
        )

        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = state,
                    onAddWeightClick = {},
                    onAddHabitClick = {},
                    onAddPhotoClick = {},
                    onViewHistoryClick = {}
                )
            }
        }

        // Verify title
        composeTestRule.onNodeWithText("Mi Mejor Versión").assertIsDisplayed()
        
        // Verify summary card content
        composeTestRule.onNodeWithText("Tu Progreso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Has registrado 2 actividades hoy").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysQuickActions() {
        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = HomeState(),
                    onAddWeightClick = {},
                    onAddHabitClick = {},
                    onAddPhotoClick = {},
                    onViewHistoryClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Registrar Peso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hábitos de Hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Foto de Progreso").assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickAddWeight_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = HomeState(),
                    onAddWeightClick = { clicked = true },
                    onAddHabitClick = {},
                    onAddPhotoClick = {},
                    onViewHistoryClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Registrar Peso").performClick()
        assert(clicked)
    }
}
