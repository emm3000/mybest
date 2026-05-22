package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.emm.mybest.R
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.ui.components.HAlertDialog
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.IconButtonVariant
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSansFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val DETAIL_SERIF_SIZE = 38.sp
private val THUMBNAIL_SIZE = 88.dp

@Composable
internal fun DayDetailContent(
    date: LocalDate,
    summary: DaySummary?,
    today: LocalDate,
    onClose: () -> Unit,
    onDeleteWeight: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onSeePhotosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == today
    var photoToDelete by remember { mutableStateOf<ProgressPhoto?>(null) }

    if (photoToDelete != null) {
        HAlertDialog(
            title = "¿Eliminar foto?",
            description = "Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            isDangerous = true,
            onDismiss = { photoToDelete = null },
            onConfirm = {
                photoToDelete?.let { onDeletePhoto(it.id) }
                photoToDelete = null
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT)
            .padding(top = 20.dp, bottom = 32.dp),
    ) {
        DetailHeader(date = date, onClose = onClose)
        Hairline(modifier = Modifier.padding(top = 12.dp))

        if (summary?.hasActivity != true) {
            DetailEmptyState()
        } else {
            DetailItems(
                summary = summary,
                isToday = isToday,
                onDeleteWeight = onDeleteWeight,
                onDeletePhoto = { photoToDelete = it },
            )
        }

        if (summary?.hasPhoto == true) {
            Surface(
                onClick = onSeePhotosClick,
                color = androidx.compose.ui.graphics.Color.Transparent,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                MicroLabel(
                    text = stringResource(R.string.history_view_photos),
                    style = MicroLabelStyle(tone = MicroLabelTone.Done),
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(date: LocalDate, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            MicroLabel(
                text = formatRecentDate(date),
                style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            )
            Text(
                text = dayNameLong(date),
                style = TextStyle(
                    fontFamily = AtelierSerifFamily,
                    fontSize = DETAIL_SERIF_SIZE,
                    fontStyle = FontStyle.Italic,
                    color = AtelierInk,
                    lineHeight = DETAIL_SERIF_SIZE,
                ),
            )
        }
        HIconButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Cerrar",
            onClick = onClose,
        )
    }
}

@Composable
private fun DetailEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        MicroLabel(
            text = stringResource(R.string.history_empty_day),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun DetailItems(
    summary: DaySummary,
    isToday: Boolean,
    onDeleteWeight: () -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
) {
    val hasWeight = summary.hasWeight
    val photos = summary.photos

    summary.weight?.let { weight ->
        DetailWeightRow(
            weightKg = weight.weight,
            note = weight.note,
            isToday = isToday,
            onDelete = onDeleteWeight,
        )
    }

    photos.forEachIndexed { index, photo ->
        val showHairline = hasWeight || index > 0
        if (showHairline) {
            Hairline(inset = HISTORY_GUT)
        }
        DetailPhotoRow(
            photo = photo,
            isToday = isToday,
            onDelete = { onDeletePhoto(photo) },
        )
    }
}

@Composable
private fun DetailWeightRow(
    weightKg: Float,
    note: String?,
    isToday: Boolean,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = stringResource(R.string.history_detail_weight_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier
                .padding(end = 14.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildWeightText(weightKg, note),
                style = TextStyle(
                    fontFamily = AtelierSansFamily,
                    fontSize = 13.sp,
                    color = AtelierInk,
                ),
            )
        }
        if (isToday) {
            HIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = "Eliminar peso",
                onClick = onDelete,
                variant = IconButtonVariant.Destructive,
            )
        }
    }
}

@Composable
private fun DetailPhotoRow(
    photo: ProgressPhoto,
    isToday: Boolean,
    onDelete: () -> Unit,
) {
    val typeLabel = photo.type.toSpanishLabel()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MicroLabel(
            text = stringResource(R.string.history_detail_photo_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier
                .padding(end = 14.dp)
                .padding(top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeLabel,
                style = TextStyle(
                    fontFamily = AtelierSansFamily,
                    fontSize = 13.sp,
                    color = AtelierInk,
                ),
            )
            AsyncImage(
                model = photo.photoPath,
                contentDescription = "Foto de ${typeLabel.lowercase()}",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(THUMBNAIL_SIZE),
                contentScale = ContentScale.Crop,
            )
        }
        if (isToday) {
            HIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = "Eliminar foto",
                onClick = onDelete,
                variant = IconButtonVariant.Destructive,
            )
        }
    }
}

private fun buildWeightText(weightKg: Float, note: String?): String {
    val base = "${"%.1f".format(weightKg)} kg"
    return if (!note.isNullOrBlank()) "$base · $note" else base
}

private val DAY_NAMES_LONG = mapOf(
    DayOfWeek.MONDAY to "Lunes",
    DayOfWeek.TUESDAY to "Martes",
    DayOfWeek.WEDNESDAY to "Miércoles",
    DayOfWeek.THURSDAY to "Jueves",
    DayOfWeek.FRIDAY to "Viernes",
    DayOfWeek.SATURDAY to "Sábado",
    DayOfWeek.SUNDAY to "Domingo",
)

private fun dayNameLong(date: LocalDate): String = DAY_NAMES_LONG[date.dayOfWeek] ?: "?"
