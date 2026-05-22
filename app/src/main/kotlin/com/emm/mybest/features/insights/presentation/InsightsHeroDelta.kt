package com.emm.mybest.features.insights.presentation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import java.util.Locale
import kotlin.math.abs

private val HERO_PADDING_TOP = 24.dp
private val DISPLAY_HERO_SIZE = 128.sp
private val MONO_KG_SIZE = 12.sp
private val MONO_KG_TRACKING = 0.16.em
private val MONO_PCT_SIZE = 10.sp
private val MONO_PCT_TRACKING = 0.14.em
private val MONO_RANGE_SIZE = 11.sp
private val MONO_RANGE_TRACKING = 0.12.em

@Composable
internal fun InsightsHeroDelta(state: InsightsState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUT_HORIZONTAL)
            .padding(top = HERO_PADDING_TOP, bottom = 8.dp),
    ) {
        MicroLabel(
            text = stringResource(R.string.insights_delta_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        if (state.deltaWeightKg != null) {
            InsightsDeltaRow(state = state)
            InsightsDeltaRange(initial = state.initialWeight, current = state.currentWeight)
        } else {
            DisplayNumber(text = "—", style = DisplayNumberStyle(fontSize = DISPLAY_HERO_SIZE, italic = true))
        }
    }
}

@Composable
private fun InsightsDeltaRow(state: InsightsState) {
    val delta = state.deltaWeightKg ?: return
    val sign = if (delta < 0f) "−" else "+"
    val formatted = "$sign${String.format(Locale.US, "%.1f", abs(delta))}"

    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DisplayNumber(
            text = formatted,
            style = DisplayNumberStyle(fontSize = DISPLAY_HERO_SIZE, italic = true),
        )
        InsightsDeltaMeta(state = state)
    }
}

@Composable
private fun InsightsDeltaMeta(state: InsightsState) {
    Column(
        modifier = Modifier.padding(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.insights_weight_unit_kg),
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_KG_SIZE,
                letterSpacing = MONO_KG_TRACKING,
                color = AtelierInkSecondary,
            ),
        )
        if (state.deltaWeightPercent != null) {
            InsightsDeltaPercent(pct = state.deltaWeightPercent)
        }
    }
}

@Composable
private fun InsightsDeltaPercent(pct: Float) {
    val pctSign = if (pct < 0f) "−" else "+"
    val pctFormatted = "$pctSign${String.format(Locale.US, "%.1f", abs(pct))}%"
    val pctColor = if (pct < 0f) AtelierDone else AtelierInk
    Text(
        text = pctFormatted,
        style = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = MONO_PCT_SIZE,
            letterSpacing = MONO_PCT_TRACKING,
            color = pctColor,
        ),
    )
}

@Composable
private fun InsightsDeltaRange(initial: Float, current: Float) {
    val initialFmt = String.format(Locale.US, "%.1f", initial)
    val currentFmt = String.format(Locale.US, "%.1f", current)

    Row(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = initialFmt,
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_RANGE_SIZE,
                letterSpacing = MONO_RANGE_TRACKING,
                color = AtelierInkTertiary,
            ),
        )
        Text(
            text = " ── ",
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_RANGE_SIZE,
                letterSpacing = MONO_RANGE_TRACKING,
                color = AtelierInkMuted,
            ),
        )
        Text(
            text = "$currentFmt KG",
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = MONO_RANGE_SIZE,
                letterSpacing = MONO_RANGE_TRACKING,
                color = AtelierInkTertiary,
            ),
        )
    }
}
