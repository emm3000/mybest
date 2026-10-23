package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val HERO_SERIF_SIZE = 46.sp
private const val HERO_SERIF_LINE_HEIGHT = 0.95f
private val MONO_10 = 10.sp
private val TRACKING_018 = 0.18.em
private val TRACKING_012 = 0.12.em

@Composable
internal fun HistoryMonthHero(
    state: HistoryState,
    modifier: Modifier = Modifier,
) {
    val monthLabel = formatMonthLabel(state.selectedMonth)
    val monthName = formatMonthName(state.selectedMonth)
    val nightsText = stringResource(R.string.history_month_outings, state.monthOutingCount, state.monthWonNightCount)
    val statsText = stringResource(R.string.history_month_stats, state.monthWeightCount, state.monthPhotoCount)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = 0.dp)
            .padding(top = 20.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        HeroLeft(monthLabel = monthLabel, monthName = monthName)
        HeroRight(navigationLabel = state.monthNavigationLabel, nightsText = nightsText, statsText = statsText)
    }
}

@Composable
private fun HeroLeft(monthLabel: String, monthName: String) {
    Column {
        MicroLabel(
            text = monthLabel,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Text(
            text = monthName,
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = HERO_SERIF_SIZE,
                fontStyle = FontStyle.Italic,
                color = AtelierInk,
                lineHeight = HERO_SERIF_SIZE * HERO_SERIF_LINE_HEIGHT,
            ),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun HeroRight(navigationLabel: String, nightsText: String, statsText: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Text(
            text = navigationLabel,
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_10,
                color = AtelierInkTertiary,
                letterSpacing = TRACKING_018,
            ),
        )
        HeroStat(text = nightsText, modifier = Modifier.padding(top = 8.dp))
        HeroStat(text = statsText, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun HeroStat(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = MONO_10,
            color = AtelierInkSecondary,
            letterSpacing = TRACKING_012,
        ),
        modifier = modifier,
    )
}
