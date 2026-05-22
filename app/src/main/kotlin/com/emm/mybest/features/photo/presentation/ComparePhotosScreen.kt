package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val PHOTO_LABEL_PADDING = 12.dp
private val PHOTO_SLOT_SPACING = 4.dp
private val EMPTY_SLOT_BACKGROUND = Color(0xFF111111)

@Composable
fun ComparePhotosScreen(
    viewModel: ComparePhotosViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ComparePhotosEffect.NavigateBack -> onBackClick()
                is ComparePhotosEffect.ShowError -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AtelierAppBar(
                title = "COMPARAR FOTOS",
                onBack = { viewModel.onIntent(ComparePhotosIntent.Close) },
            )
        },
        containerColor = Color.Black,
    ) { padding ->
        ComparePhotosBody(
            before = state.before,
            after = state.after,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun ComparePhotosBody(
    before: ProgressPhoto?,
    after: ProgressPhoto?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(PHOTO_SLOT_SPACING),
        ) {
            ComparePhotoSlot(
                photo = before,
                label = "ANTES",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            ComparePhotoSlot(
                photo = after,
                label = "AHORA",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun ComparePhotoSlot(
    photo: ProgressPhoto?,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (photo != null) {
            AsyncImage(
                model = photo.photoPath,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            MicroLabel(
                text = photo.date.formatDdMmYy(),
                style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(PHOTO_LABEL_PADDING),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EMPTY_SLOT_BACKGROUND),
                contentAlignment = Alignment.Center,
            ) {
                MicroLabel(
                    text = label,
                    style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                )
            }
        }
    }
}
