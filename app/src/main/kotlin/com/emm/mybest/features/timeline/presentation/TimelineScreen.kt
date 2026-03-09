package com.emm.mybest.features.timeline.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
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
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HTopBar

private const val TIMELINE_PHOTO_HEIGHT_RATIO = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HTopBar(title = "Línea de tiempo")
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
        Column(
            modifier = contentModifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp,
            )
            HSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(width = 240.dp, height = 72.dp),
                cornerRadius = 20.dp,
            )
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
                        text = "Tipo: ${timelinePhotoTypeLabel(currentPhoto.type)}",
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
            contentDescription = "Foto de ${timelinePhotoTypeLabel(
                photo.type,
            ).lowercase()} del ${photo.date.formatEsLongDate()}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

private fun timelinePhotoTypeLabel(type: PhotoType): String = when (type) {
    PhotoType.FACE -> "Cara"
    PhotoType.ABDOMEN -> "Abdomen"
    PhotoType.BODY -> "Cuerpo"
    PhotoType.BREAKFAST -> "Desayuno"
    PhotoType.LUNCH -> "Almuerzo"
    PhotoType.DINNER -> "Cena"
    PhotoType.FOOD -> "Comida"
}
