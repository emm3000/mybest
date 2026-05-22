package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.domain.media.MediaManager
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.features.photo.presentation.components.CompareFooter
import com.emm.mybest.features.photo.presentation.components.CompareHero
import com.emm.mybest.features.photo.presentation.components.PhotoTypeTabs
import com.emm.mybest.features.photo.presentation.components.TimelineRail
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.atelier.FaceSilhouette
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.TrunkSilhouette

@Composable
fun PhotosScreen(
    viewModel: PhotosViewModel,
    mediaManager: MediaManager,
    onOpenViewer: (String) -> Unit,
    onCompare: (String, String) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraHandler = rememberPhotoCameraHandler(
        snackbarHostState = snackbarHostState,
        mediaManager = mediaManager,
        onPhotoCaptured = { uri -> viewModel.onIntent(PhotosIntent.OnPhotoCaptured(uri)) },
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PhotosEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is PhotosEffect.NavigateToCompare -> onCompare(effect.beforeId, effect.afterId)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        topBar = {
            AtelierAppBar(
                title = stringResource(R.string.photos_app_bar_title),
                actions = {
                    HIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(R.string.photos_add_action_cd),
                        onClick = { cameraHandler.launch() },
                    )
                },
            )
        },
        bottomBar = bottomBar,
    ) { paddingValues ->
        PhotosContent(
            state = state,
            onSelectType = { viewModel.onIntent(PhotosIntent.SelectType(it)) },
            onOpenViewer = onOpenViewer,
            onCompare = { viewModel.onIntent(PhotosIntent.OpenCompare) },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun PhotosContent(
    state: PhotosState,
    onSelectType: (PhotoType) -> Unit,
    onOpenViewer: (String) -> Unit,
    onCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val silhouette = if (state.selectedType == PhotoType.TRUNK) TrunkSilhouette else FaceSilhouette

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        PhotoTypeTabs(
            selectedType = state.selectedType,
            onSelectType = onSelectType,
        )
        Hairline()
        CompareHero(
            beforePhoto = state.overview.before,
            afterPhoto = state.overview.after,
            silhouette = silhouette,
        )
        CompareFooter(
            beforePhoto = state.overview.before,
            afterPhoto = state.overview.after,
            beforeWeightKg = state.overview.beforeWeightKg,
            afterWeightKg = state.overview.afterWeightKg,
            today = state.today,
            onCompareClick = onCompare,
        )
        Hairline()
        TimelineRail(
            photos = state.overview.timeline,
            onTileClick = onOpenViewer,
        )
    }
}
