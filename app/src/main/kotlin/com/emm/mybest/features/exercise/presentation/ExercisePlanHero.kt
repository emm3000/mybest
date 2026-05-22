package com.emm.mybest.features.exercise.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val HERO_PADDING_TOP = 24.dp
private val HERO_PADDING_BOTTOM = 22.dp
private val HERO_MARGIN_TOP = 4.dp
private val HERO_PADDING_HORIZONTAL = 28.dp
private val HERO_FONT_SIZE = 56.sp
private val HERO_LINE_HEIGHT = 53.2.sp

@Composable
internal fun ExercisePlanHero() {
    Column(
        modifier = Modifier.padding(
            start = HERO_PADDING_HORIZONTAL,
            end = HERO_PADDING_HORIZONTAL,
            top = HERO_PADDING_TOP,
            bottom = HERO_PADDING_BOTTOM,
        ),
    ) {
        MicroLabel(
            text = stringResource(R.string.exercise_plan_hero_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Text(
            text = stringResource(R.string.exercise_plan_hero_line1),
            modifier = Modifier.padding(top = HERO_MARGIN_TOP),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontStyle = FontStyle.Italic,
                fontSize = HERO_FONT_SIZE,
                lineHeight = HERO_LINE_HEIGHT,
                color = AtelierInk,
            ),
        )
        Text(
            text = stringResource(R.string.exercise_plan_hero_line2),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = HERO_FONT_SIZE,
                lineHeight = HERO_LINE_HEIGHT,
                color = AtelierInkTertiary,
            ),
        )
    }
}
