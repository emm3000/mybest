package com.emm.mybest.features.onboarding.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierTheme

private val PAGE_GUTTER = 28.dp
private val TITLE_TOP = 40.dp
private val SECTION_GAP = 24.dp
private val LINE_VERTICAL_PADDING = 16.dp

@Composable
fun OnboardingStepPage(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OnboardingTitle(titleRes = step.titleRes)
        OnboardingLines(lineResIds = step.lineResIds)
        Text(
            text = stringResource(step.whyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = AtelierInkSecondary,
            modifier = Modifier.padding(horizontal = PAGE_GUTTER, vertical = SECTION_GAP),
        )
        body()
        step.noteRes?.let { OnboardingNote(noteRes = it) }
    }
}

@Composable
private fun OnboardingNote(@StringRes noteRes: Int) {
    Text(
        text = stringResource(noteRes),
        style = MaterialTheme.typography.bodyMedium,
        color = AtelierInkTertiary,
        modifier = Modifier.padding(horizontal = PAGE_GUTTER, vertical = SECTION_GAP),
    )
}

@Composable
private fun OnboardingTitle(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.headlineLarge,
        color = AtelierInk,
        modifier = Modifier.padding(start = PAGE_GUTTER, end = PAGE_GUTTER, top = TITLE_TOP),
    )
}

@Composable
private fun OnboardingLines(lineResIds: List<Int>) {
    if (lineResIds.isEmpty()) return
    Column(modifier = Modifier.padding(top = SECTION_GAP)) {
        lineResIds.forEach { lineRes ->
            Hairline()
            Text(
                text = stringResource(lineRes),
                style = MaterialTheme.typography.bodyLarge,
                color = AtelierInk,
                modifier = Modifier.padding(horizontal = PAGE_GUTTER, vertical = LINE_VERTICAL_PADDING),
            )
        }
        Hairline()
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun OnboardingStepPagePreview() {
    AtelierTheme {
        OnboardingStepPage(step = OnboardingStep.CHECKS)
    }
}
