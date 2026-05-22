package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierHairline

private val PHOTOS_PADDING_VERTICAL = 18.dp
private val PHOTOS_DIVIDER_INSET = 22.dp
private val PHOTO_COUNT_CELL_HEIGHT = 96.dp
private val DISPLAY_PHOTO_SIZE = 36.sp

@Composable
internal fun InsightsPhotoCounts(tronco: Int, cara: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(PHOTO_COUNT_CELL_HEIGHT)
            .padding(horizontal = GUT_HORIZONTAL, vertical = PHOTOS_PADDING_VERTICAL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InsightsPhotoCell(
            label = stringResource(R.string.insights_photo_tronco_label),
            count = tronco,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = PHOTOS_DIVIDER_INSET)
                .width(1.dp)
                .fillMaxHeight()
                .background(AtelierHairline),
        )
        InsightsPhotoCell(
            label = stringResource(R.string.insights_photo_cara_label),
            count = cara,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InsightsPhotoCell(label: String, count: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        MicroLabel(text = label, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        DisplayNumber(
            text = count.toString(),
            style = DisplayNumberStyle(fontSize = DISPLAY_PHOTO_SIZE),
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
