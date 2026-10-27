package com.emm.mybest.features.photo.presentation.viewer

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private fun photoOn(id: String, day: Int) = ProgressPhoto(
    id = id,
    date = LocalDate(2026, 11, day),
    type = PhotoType.TRUNK,
    photoPath = "",
    createdAt = day.toLong(),
)

private fun loadedState(initialPhotoId: String, initialPage: Int) = PhotoViewerState(
    photos = listOf(photoOn("c", 11), photoOn("b", 10), photoOn("a", 9)),
    initialPhotoId = initialPhotoId,
    isLoading = false,
    initialPage = initialPage,
)

class PhotoViewerContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: State<PhotoViewerState>) {
        composeTestRule.setContent {
            AtelierTheme {
                PhotoViewerContent(
                    state = state.value,
                    snackbarHostState = SnackbarHostState(),
                    actions = PhotoViewerActions(onBack = {}, onReplace = {}),
                )
            }
        }
    }

    @Test
    fun photoViewerContent_opensOnTappedPhotoWhenPhotosArriveAfterFirstFrame() {
        val state = mutableStateOf(PhotoViewerState(initialPhotoId = "a", isLoading = true))
        setContent(state)

        state.value = loadedState(initialPhotoId = "a", initialPage = 2)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("9 de noviembre, 2026").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithText("11 de noviembre, 2026").fetchSemanticsNodes().size)
    }

    @Test
    fun photoViewerContent_opensOnInitialPageWhenLoadedFromTheStart() {
        setContent(mutableStateOf(loadedState(initialPhotoId = "b", initialPage = 1)))

        composeTestRule.onNodeWithText("10 de noviembre, 2026").assertIsDisplayed()
    }
}
