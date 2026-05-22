package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily

internal val RECO_TITLE_SIZE = 26.sp
internal val RECO_TITLE_LINE_HEIGHT = 30.7.sp
internal val MONO_FOOTER_SIZE = 10.sp
internal val MONO_FOOTER_TRACKING = 0.16.em
internal val GUT_HORIZONTAL = 28.dp

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HandleInsightsEffects(
        viewModel = viewModel,
        onHistoryClick = onHistoryClick,
        onAddWeightClick = onAddWeightClick,
    )

    Scaffold(
        modifier = modifier,
        containerColor = AtelierBackground,
    ) { padding ->
        InsightsContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun HandleInsightsEffects(
    viewModel: InsightsViewModel,
    onHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
) {
    val currentOnHistoryClick by rememberUpdatedState(onHistoryClick)
    val currentOnAddWeightClick by rememberUpdatedState(onAddWeightClick)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InsightsEffect.NavigateToHistory -> currentOnHistoryClick()
                InsightsEffect.NavigateToAddWeight -> currentOnAddWeightClick()
            }
        }
    }
}

@Composable
private fun InsightsContent(
    state: InsightsState,
    onIntent: (InsightsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> InsightsLoading(modifier = modifier)
        state.errorMessage != null -> InsightsError(message = state.errorMessage, modifier = modifier)
        state.weightHistory.isEmpty() -> InsightsEmpty(onIntent = onIntent, modifier = modifier)
        else -> InsightsLoaded(state = state, modifier = modifier)
    }
}

@Composable
private fun InsightsLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        MicroLabel(
            text = stringResource(R.string.insights_loading),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun InsightsError(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MicroLabel(text = "ERROR", style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        Text(
            text = message,
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic,
                color = AtelierInkTertiary,
                lineHeight = 24.sp,
            ),
            modifier = Modifier.padding(top = 8.dp, start = GUT_HORIZONTAL, end = GUT_HORIZONTAL),
        )
    }
}

@Composable
private fun InsightsEmpty(onIntent: (InsightsIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MicroLabel(
            text = stringResource(R.string.insights_empty_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Text(
            text = stringResource(R.string.insights_empty_hero),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = RECO_TITLE_SIZE,
                fontStyle = FontStyle.Italic,
                color = AtelierInk,
                lineHeight = RECO_TITLE_LINE_HEIGHT,
            ),
            modifier = Modifier.padding(top = 8.dp),
        )
        InsightsCtaButton(
            label = stringResource(R.string.insights_register_weight_cta),
            onClick = { onIntent(InsightsIntent.OnAddWeightClick) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
internal fun InsightsCtaButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = AtelierInk,
        contentColor = AtelierBackground,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_FOOTER_SIZE,
                letterSpacing = MONO_FOOTER_TRACKING,
                color = AtelierBackground,
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}
