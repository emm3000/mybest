package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.core.datetime.longEs
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HAlertDialog
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.LocalDate

private val DETAIL_SERIF_SIZE = 38.sp

@Composable
internal fun DayDetailContent(
    detail: DayDetailState,
    callbacks: DayDetailCallbacks,
    modifier: Modifier = Modifier,
) {
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
                photoToDelete?.let { callbacks.onDeletePhoto(it.id) }
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
        DetailHeader(date = detail.date, onClose = callbacks.onClose)
        Hairline(modifier = Modifier.padding(top = 12.dp))

        if (detail.isEmpty) {
            DetailEmptyState()
        } else {
            DetailSections(
                detail = detail,
                onDeleteWeight = callbacks.onDeleteWeight,
                photoActions = { photo ->
                    PhotoRowActions(
                        onReplace = { callbacks.onReplacePhoto(photo) },
                        onDelete = { photoToDelete = photo },
                    )
                },
            )
        }

        if (detail.photos.isNotEmpty()) {
            Surface(
                onClick = callbacks.onSeePhotosClick,
                color = Color.Transparent,
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
                text = date.dayOfWeek.longEs(),
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
private fun DetailSections(
    detail: DayDetailState,
    onDeleteWeight: () -> Unit,
    photoActions: (ProgressPhoto) -> PhotoRowActions,
) {
    detail.outing?.let { HistoryOutingDetail(outing = it) }
    detail.wonNight?.let { HistoryWonNightDetail(wonNight = it) }
    HistoryChecksDetail(checks = detail.checks)
    detail.weight?.let { DetailWeightRow(row = it, onDelete = onDeleteWeight) }
    detail.photos.forEach { row -> DetailPhotoRow(row = row, actions = photoActions(row.photo)) }
}
