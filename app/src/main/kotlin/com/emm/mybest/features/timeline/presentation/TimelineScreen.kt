package com.emm.mybest.features.timeline.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HTopBar

private const val TIMELINE_PHOTO_HEIGHT_RATIO = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    val currentOnBackClick by androidx.compose.runtime.rememberUpdatedState(onBackClick)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TimelineEffect.NavigateBack -> currentOnBackClick()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HTopBar(
                title = "Línea de Tiempo",
                navigationIcon = {
                    IconButton(onClick = { viewModel.onIntent(TimelineIntent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Regresar",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        TimelineContent(
            state = state,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimelineContent(
    state: TimelineState,
    modifier: Modifier = Modifier,
) {
    val contentModifier = modifier.fillMaxSize()

    if (state.isLoading) {
        Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    if (state.photosByDate.isEmpty()) {
        HEmptyState(
            title = "Sin recuerdos aún",
            description = "Tus fotos de progreso aparecerán aquí para que veas tu evolución.",
            icon = Icons.Rounded.PhotoCamera,
            modifier = contentModifier,
        )
        return
    }

    val allPhotos = state.photosByDate.values.flatten().sortedByDescending { it.createdAt }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { allPhotos.size })

    Column(modifier = modifier.fillMaxSize()) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
        ) { page ->
            val photo = allPhotos[page]
            PhotoPagerItem(photo)
        }

        // Pager Indicators / Info
        val currentPhoto = allPhotos[pagerState.currentPage]
        HCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            variant = CardVariant.Filled,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = currentPhoto.date.formatEsLongDate(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Evolución: ${currentPhoto.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${allPhotos.size}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun PhotoPagerItem(
    photo: ProgressPhoto,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(TIMELINE_PHOTO_HEIGHT_RATIO),
        variant = CardVariant.Elevated,
        cornerRadius = 24.dp,
        shadowElevation = 8.dp,
    ) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
