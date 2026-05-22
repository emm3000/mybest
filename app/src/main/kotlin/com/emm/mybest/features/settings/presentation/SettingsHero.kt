package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val HERO_LABEL_TOP = 24.dp
private val HERO_TITLE_TOP = 8.dp
private val HERO_BODY_TOP = 10.dp
private val HERO_BOTTOM = 22.dp
private val SETTINGS_GUT = 28.dp
private val HERO_TITLE_SIZE = 40.sp
private val HERO_BODY_SIZE = 13.sp
private val HERO_BODY_LINE_HEIGHT = 19.5.sp

@Composable
internal fun SettingsHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = SETTINGS_GUT, end = SETTINGS_GUT, top = HERO_LABEL_TOP, bottom = HERO_BOTTOM),
    ) {
        MicroLabel(
            text = stringResource(R.string.settings_account_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Text(
            text = stringResource(R.string.settings_account_hero),
            modifier = Modifier.padding(top = HERO_TITLE_TOP),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontStyle = FontStyle.Italic,
                fontSize = HERO_TITLE_SIZE,
                lineHeight = HERO_TITLE_SIZE,
            ),
        )
        Text(
            text = stringResource(R.string.settings_account_body),
            modifier = Modifier.padding(top = HERO_BODY_TOP),
            style = TextStyle(
                fontSize = HERO_BODY_SIZE,
                lineHeight = HERO_BODY_LINE_HEIGHT,
                color = AtelierInkSecondary,
            ),
        )
    }
}
