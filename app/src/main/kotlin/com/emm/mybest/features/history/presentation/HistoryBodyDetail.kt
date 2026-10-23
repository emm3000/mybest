package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.emm.mybest.R
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.IconButtonVariant
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.components.atelier.photoTypeLabel
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSansFamily

private val THUMBNAIL_SIZE = 88.dp
private val ROW_TOP_PADDING = 16.dp
private val LABEL_END_PADDING = 14.dp
private val PHOTO_LABEL_TOP_PADDING = 2.dp
private val PHOTO_TOP_PADDING = 8.dp
private val BODY_TEXT_STYLE = TextStyle(
    fontFamily = AtelierSansFamily,
    fontSize = 13.sp,
    color = AtelierInk,
)

@Composable
internal fun DetailWeightRow(row: WeightRowState, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ROW_TOP_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BodyRowLabel(text = stringResource(R.string.history_detail_weight_label))
        Text(text = row.label, style = BODY_TEXT_STYLE, modifier = Modifier.weight(1f))
        BodyRowDelete(
            canDelete = row.canDelete,
            description = stringResource(R.string.history_delete_weight),
            onDelete = onDelete,
        )
    }
}

@Composable
internal fun DetailPhotoRow(row: PhotoRowState, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Hairline()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ROW_TOP_PADDING),
        verticalAlignment = Alignment.Top,
    ) {
        BodyRowLabel(
            text = stringResource(R.string.history_detail_photo_label),
            modifier = Modifier.padding(top = PHOTO_LABEL_TOP_PADDING),
        )
        PhotoColumn(photo = row.photo, modifier = Modifier.weight(1f))
        BodyRowDelete(
            canDelete = row.canDelete,
            description = stringResource(R.string.history_delete_photo),
            onDelete = onDelete,
        )
    }
}

@Composable
private fun PhotoColumn(photo: ProgressPhoto, modifier: Modifier = Modifier) {
    val typeLabel = photoTypeLabel(photo.type)
    Column(modifier = modifier) {
        Text(text = typeLabel, style = BODY_TEXT_STYLE)
        AsyncImage(
            model = photo.photoPath,
            contentDescription = stringResource(R.string.history_photo_description, typeLabel.lowercase()),
            modifier = Modifier
                .padding(top = PHOTO_TOP_PADDING)
                .size(THUMBNAIL_SIZE),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun BodyRowLabel(text: String, modifier: Modifier = Modifier) {
    MicroLabel(
        text = text,
        style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        modifier = modifier.padding(end = LABEL_END_PADDING),
    )
}

@Composable
private fun BodyRowDelete(canDelete: Boolean, description: String, onDelete: () -> Unit) {
    if (!canDelete) return
    HIconButton(
        icon = Icons.Rounded.Delete,
        contentDescription = description,
        onClick = onDelete,
        variant = IconButtonVariant.Destructive,
    )
}
