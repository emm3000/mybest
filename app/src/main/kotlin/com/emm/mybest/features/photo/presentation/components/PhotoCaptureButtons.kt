package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierTheme

private val BUTTON_MIN_HEIGHT = 48.dp
private val BUTTON_HORIZONTAL_PADDING = 28.dp
private val BUTTON_VERTICAL_PADDING = 12.dp
private val BUTTON_GAP = 8.dp

@Composable
internal fun PhotoCaptureButtons(
    onTakePhoto: (PhotoType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.navigationBarsPadding()) {
        Hairline()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BUTTON_HORIZONTAL_PADDING, vertical = BUTTON_VERTICAL_PADDING),
            verticalArrangement = Arrangement.spacedBy(BUTTON_GAP),
        ) {
            HButton(
                text = stringResource(R.string.photos_take_trunk),
                onClick = { onTakePhoto(PhotoType.TRUNK) },
                modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
                variant = ButtonVariant.Default,
            )
            HButton(
                text = stringResource(R.string.photos_take_face),
                onClick = { onTakePhoto(PhotoType.FACE) },
                modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
                variant = ButtonVariant.Default,
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun PhotoCaptureButtonsPreview() {
    AtelierTheme {
        PhotoCaptureButtons(onTakePhoto = {})
    }
}
