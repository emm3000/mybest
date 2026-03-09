package com.emm.mybest.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.emm.mybest.features.home.presentation.HomeIntent
import com.emm.mybest.features.home.presentation.HomeScreenContent
import com.emm.mybest.features.home.presentation.HomeState
import com.emm.mybest.ui.theme.MyBestTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysTitleAndSummary() {
        val state = HomeState(
            lastWeight = 70.0f,
            totalWeightLost = 2.0f,
            totalPhotos = 5,
            isLoading = false
        )

        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = state,
                    onIntent = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        // Verify title
        composeTestRule.onNodeWithText("Mi Mejor Versión").assertIsDisplayed()
        
        // Verify summary card content
        composeTestRule.onNodeWithText("Tu Progreso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Toca para ver tus estadísticas").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysQuickActions() {
        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = HomeState(),
                    onIntent = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        composeTestRule.onNodeWithText("Registrar Peso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Crear Hábito").assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickAddWeight_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyBestTheme {
                HomeScreenContent(
                    state = HomeState(),
                    onIntent = { intent ->
                        if (intent is HomeIntent.OnAddWeightClick) clicked = true
                    },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        composeTestRule.onNodeWithText("Registrar Peso").performClick()
        assert(clicked)
    }
}
