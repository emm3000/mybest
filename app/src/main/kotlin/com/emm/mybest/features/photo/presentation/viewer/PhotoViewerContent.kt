package com.emm.mybest.features.photo.presentation.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.atelier.photoTypeLabel
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.datetime.LocalDate

@Composable
internal fun PhotoViewerContent(
    state: PhotoViewerState,
    snackbarHostState: SnackbarHostState,
    actions: PhotoViewerActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (state.isLoading) {
            ViewerOverlay(currentPhoto = null, onBack = actions.onBack)
        } else {
            LoadedPhotoViewer(state = state, snackbarHostState = snackbarHostState, actions = actions)
        }
    }
}

@Composable
private fun LoadedPhotoViewer(
    state: PhotoViewerState,
    snackbarHostState: SnackbarHostState,
    actions: PhotoViewerActions,
    modifier: Modifier = Modifier,
) {
    val photos = state.photos
    val pagerState = rememberPagerState(initialPage = state.initialPage) { photos.size }
    var isOverlayVisible by remember { mutableStateOf(true) }
    val currentPhoto = photos.getOrNull(pagerState.currentPage)

    Box(modifier = modifier.fillMaxSize()) {
        PhotoPager(
            photos = photos,
            pagerState = pagerState,
            onToggleOverlay = { isOverlayVisible = !isOverlayVisible },
        )
        AnimatedVisibility(
            visible = isOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            ViewerOverlay(currentPhoto = currentPhoto, onBack = actions.onBack)
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            HSnackbarHost(hostState = snackbarHostState)
            AnimatedVisibility(
                visible = isOverlayVisible && currentPhoto != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ViewerBottomBar(onReplace = { currentPhoto?.let(actions.onReplace) })
            }
        }
    }
}

@Composable
private fun PhotoPager(
    photos: List<ProgressPhoto>,
    pagerState: PagerState,
    onToggleOverlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleOverlay,
            ),
    ) { page ->
        val photo = photos[page]
        AsyncImage(
            model = photo.photoPath,
            contentDescription = photoTypeLabel(photo.type),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun PhotoViewerContentPreview() {
    val photo = ProgressPhoto(
        id = "p1",
        date = LocalDate(2026, 11, 9),
        type = PhotoType.TRUNK,
        photoPath = "",
        createdAt = 1_000L,
    )
    AtelierTheme {
        PhotoViewerContent(
            state = PhotoViewerState(photos = listOf(photo), initialPhotoId = "p1", isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            actions = PhotoViewerActions(onBack = {}, onReplace = {}),
        )
    }
}
