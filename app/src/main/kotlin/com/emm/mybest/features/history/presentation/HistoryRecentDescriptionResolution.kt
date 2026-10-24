package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R

@Composable
internal fun HistoryRecentDescription.resolve(): String {
    val separator = stringResource(R.string.history_recent_separator)
    val weightPart = weight?.let { stringResource(R.string.history_recent_weight, it) }
    val photoLabels = photoLabelResIds.map { stringResource(it) }
    val photoPart = photoLabels.takeIf { it.isNotEmpty() }
        ?.let { stringResource(R.string.history_recent_photos, it.joinToString(separator)) }
    return listOfNotNull(weightPart, photoPart).joinToString(separator)
}
