package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

private val HEADER_PADDING_TOP = 18.dp
private val HEADER_PADDING_BOTTOM = 14.dp

@Composable
internal fun InsightsLoaded(state: InsightsState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        InsightsHeader(daysSinceFirstWeight = state.daysSinceFirstWeight)
        Hairline()
        InsightsHeroDelta(state = state)
        val weights: ImmutableList<Float> = remember(state.weightHistory) {
            state.weightHistory.map { it.weight }.toImmutableList()
        }
        InsightsMiniChartContainer(weights = weights)
        Hairline()
        InsightsPhotoCounts(tronco = state.troncoPhotoCount, cara = state.caraPhotoCount)
        Hairline()
        InsightsRecommendation(
            title = state.recommendationTitle,
            description = state.recommendationDescription,
            kgPerDayRate14d = state.kgPerDayRate14d,
        )
    }
}

@Composable
private fun InsightsHeader(daysSinceFirstWeight: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUT_HORIZONTAL)
            .padding(top = HEADER_PADDING_TOP, bottom = HEADER_PADDING_BOTTOM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(text = stringResource(R.string.insights_header_title))
        if (daysSinceFirstWeight != null) {
            MicroLabel(
                text = pluralStringResource(R.plurals.insights_days_format, daysSinceFirstWeight, daysSinceFirstWeight),
                style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0C)
@Composable
private fun InsightsLoadedPreview() {
    val sampleState = InsightsState(
        weightHistory = persistentListOf(
            WeightEntry(id = "1", date = LocalDate(2023, 10, 1), weight = 85.0f),
            WeightEntry(id = "2", date = LocalDate(2023, 10, 15), weight = 82.5f),
            WeightEntry(id = "3", date = LocalDate(2023, 11, 1), weight = 81.0f),
        ),
        totalWeightLost = 4.0f,
        currentWeight = 81.0f,
        initialWeight = 85.0f,
        deltaWeightKg = -4.0f,
        deltaWeightPercent = -4.7f,
        kgPerDayRate14d = -0.1f,
        recommendationTitle = "Keep it up!",
        recommendationDescription = "You're doing great with your progress.",
        daysSinceFirstWeight = 31,
        troncoPhotoCount = 5,
        caraPhotoCount = 2,
        isLoading = false,
    )
    AtelierTheme {
        InsightsLoaded(state = sampleState)
    }
}
