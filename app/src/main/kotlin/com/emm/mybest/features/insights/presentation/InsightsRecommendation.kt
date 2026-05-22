package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
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
import java.util.Locale
import kotlin.math.abs

private val RECO_PADDING_TOP = 22.dp
private val RECO_PADDING_BOTTOM = 24.dp
private val RECO_DESC_SIZE = 13.sp
private val RECO_DESC_LINE_HEIGHT = 19.sp
private val RECO_MAX_WIDTH = 320.dp

@Composable
internal fun InsightsRecommendation(
    title: String,
    description: String,
    kgPerDayRate14d: Float?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUT_HORIZONTAL)
            .padding(top = RECO_PADDING_TOP, bottom = RECO_PADDING_BOTTOM),
    ) {
        MicroLabel(
            text = stringResource(R.string.insights_recommendation_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Warm),
        )
        Text(
            text = title,
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = RECO_TITLE_SIZE,
                fontStyle = FontStyle.Italic,
                color = AtelierInk,
                lineHeight = RECO_TITLE_LINE_HEIGHT,
            ),
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = description,
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = RECO_DESC_SIZE,
                color = AtelierInkSecondary,
                lineHeight = RECO_DESC_LINE_HEIGHT,
            ),
            modifier = Modifier
                .widthIn(max = RECO_MAX_WIDTH)
                .padding(top = 8.dp),
        )
        InsightsRecommendationFooter(kgPerDayRate14d = kgPerDayRate14d)
    }
}

@Composable
private fun InsightsRecommendationFooter(kgPerDayRate14d: Float?) {
    val footerText = if (kgPerDayRate14d != null) {
        val rateStr = String.format(Locale.US, "%.2f", abs(kgPerDayRate14d))
        stringResource(R.string.insights_recommendation_basis_with_rate_format, rateStr)
    } else {
        stringResource(R.string.insights_recommendation_basis_no_rate)
    }
    Text(
        text = footerText,
        style = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = MONO_FOOTER_SIZE,
            letterSpacing = MONO_FOOTER_TRACKING,
            color = AtelierInkTertiary,
        ),
        modifier = Modifier.padding(top = 14.dp),
    )
}
