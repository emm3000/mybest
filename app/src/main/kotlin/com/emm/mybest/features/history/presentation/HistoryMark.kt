package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierWarm

private val MARK_SIZE = 4.dp
private val RING_WIDTH = 1.dp

@Composable
internal fun HistoryMark(kind: HistoryMarkKind, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(MARK_SIZE).then(kind.fill()))
}

@Composable
private fun HistoryMarkKind.fill(): Modifier = when (this) {
    HistoryMarkKind.CHECK -> Modifier.background(AtelierDone)
    HistoryMarkKind.OUTING -> Modifier.background(AtelierInk)
    HistoryMarkKind.RELAPSE -> Modifier.background(MaterialTheme.colorScheme.error)
    HistoryMarkKind.WON_NIGHT -> Modifier.background(AtelierWarm)
    HistoryMarkKind.WEIGHT -> Modifier.border(RING_WIDTH, AtelierInkSecondary)
    HistoryMarkKind.PHOTO -> Modifier.border(RING_WIDTH, AtelierDone)
}
