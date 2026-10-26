package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val CONTENT_GUTTER = 24.dp
private val SECTION_GAP = 28.dp
private val FOOTER_GAP = 12.dp

@Composable
internal fun CheckInPage(
    footer: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CONTENT_GUTTER, vertical = SECTION_GAP),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SECTION_GAP), content = content)
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(top = SECTION_GAP),
            verticalArrangement = Arrangement.spacedBy(FOOTER_GAP),
            content = footer,
        )
    }
}
