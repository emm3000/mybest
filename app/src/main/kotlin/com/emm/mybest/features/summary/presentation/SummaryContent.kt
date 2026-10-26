package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme

private val CONTENT_PADDING = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
private val SECTION_GAP = 26.dp
private val TITLE_PADDING = 4.dp

@Composable
fun SummaryContent(
    state: SummaryState,
    onIntent: (SummaryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = CONTENT_PADDING,
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
    ) {
        item { SummaryHeader(state = state, onIntent = onIntent) }
        items(items = state.sections, key = { it.titleRes }) { section ->
            SummarySection(section = section, onRowClick = { onIntent(SummaryIntent.OnRowClick(it)) })
        }
    }
}

@Composable
private fun SummaryHeader(
    state: SummaryState,
    onIntent: (SummaryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MicroLabel(text = state.weekLabel)
        SummaryWeekNavigation(canGoNext = state.canGoNext, onIntent = onIntent)
    }
}

@Composable
private fun SummarySection(
    section: SummarySectionState,
    onRowClick: (SummaryDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MicroLabel(text = stringResource(section.titleRes), modifier = Modifier.padding(bottom = TITLE_PADDING))
        Hairline()
        section.rows.forEach { SummaryRow(row = it, onRowClick = onRowClick) }
    }
}

private val PREVIEW_STATE = SummaryState(
    weekLabel = "19 OCT – 25 OCT",
    canGoNext = false,
    sections = listOf(
        SummarySectionState(
            titleRes = R.string.summary_section_outings,
            rows = listOf(plainLine(R.string.summary_outings_count, "1")),
        ),
        SummarySectionState(
            titleRes = R.string.summary_section_checks,
            rows = listOf(
                SummaryRowState.SetupPrompt(SummaryText(R.string.summary_checks_setup), SummaryDestination.SETTINGS),
            ),
        ),
    ),
)

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun SummaryContentPreview() {
    AtelierTheme {
        SummaryContent(state = PREVIEW_STATE, onIntent = {})
    }
}
