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
import com.emm.mybest.core.datetime.YearMonthValue
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
    month: YearMonthValue,
    weightCount: Int,
    photoCount: Int,
    modifier: Modifier = Modifier,
) {
    val monthLabel = formatMonthLabel(month)
    val monthName = formatMonthName(month)
    val statsText = stringResource(R.string.history_month_stats, weightCount, photoCount)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = 0.dp)
            .padding(top = 20.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        HeroLeft(monthLabel = monthLabel, monthName = monthName)
        HeroRight(statsText = statsText)
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
private fun HeroRight(statsText: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Text(
            text = "← ABR · JUN →",
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_10,
                color = AtelierInkTertiary,
                letterSpacing = TRACKING_018,
            ),
        )
        Text(
            text = statsText,
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_10,
                color = AtelierInkSecondary,
                letterSpacing = TRACKING_012,
            ),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
