package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val TAB_HEIGHT = 48.dp
private val TAB_HORIZONTAL_PADDING = 28.dp
private val TAB_GAP = 22.dp
private const val TAB_FONT_SIZE = 22

@Composable
internal fun PhotoTypeTabs(
    selectedType: PhotoType,
    onSelectType: (PhotoType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TAB_HORIZONTAL_PADDING, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(TAB_GAP),
    ) {
        PhotoTypeTab(
            label = stringResource(R.string.photos_tab_trunk),
            isActive = selectedType == PhotoType.TRUNK,
            onClick = { onSelectType(PhotoType.TRUNK) },
        )
        PhotoTypeTab(
            label = stringResource(R.string.photos_tab_face),
            isActive = selectedType == PhotoType.FACE,
            onClick = { onSelectType(PhotoType.FACE) },
        )
    }
}

@Composable
private fun PhotoTypeTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineColor = AtelierInk
    Box(
        modifier = modifier
            .height(TAB_HEIGHT)
            .clickable(onClick = onClick)
            .then(
                if (isActive) {
                    Modifier.drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height - strokeWidth / 2f),
                            end = Offset(size.width, size.height - strokeWidth / 2f),
                            strokeWidth = strokeWidth,
                        )
                    }
                } else {
                    Modifier
                },
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = AtelierSerifFamily,
            fontSize = TAB_FONT_SIZE.sp,
            color = if (isActive) AtelierInk else AtelierInkTertiary,
        )
    }
}
