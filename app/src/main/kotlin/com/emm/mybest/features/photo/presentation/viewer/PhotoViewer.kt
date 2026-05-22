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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.atelier.photoTypeLabel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PhotoViewer(
    initialPhotoId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PhotoViewerViewModel = koinViewModel { parametersOf(initialPhotoId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val photos = state.photos
    val initialPage = photos.indexOfFirst { it.id == state.initialPhotoId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { photos.size }
    var overlayVisible by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        PhotoPager(
            photos = photos,
            pagerState = pagerState,
            onToggleOverlay = { overlayVisible = !overlayVisible },
        )
        val currentPhoto = photos.getOrNull(pagerState.currentPage)
        AnimatedVisibility(
            visible = overlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            ViewerOverlay(currentPhoto = currentPhoto, onBack = onBack)
        }
    }
}

@Composable
private fun PhotoPager(
    photos: List<ProgressPhoto>,
    pagerState: PagerState,
    onToggleOverlay: () -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
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

@Composable
private fun ViewerOverlay(
    currentPhoto: ProgressPhoto?,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                )
            }
            if (currentPhoto != null) {
                ViewerPhotoInfo(
                    photo = currentPhoto,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun ViewerPhotoInfo(
    photo: ProgressPhoto,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = photo.date.formatEsLongDate(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = photoTypeLabel(photo.type),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}
