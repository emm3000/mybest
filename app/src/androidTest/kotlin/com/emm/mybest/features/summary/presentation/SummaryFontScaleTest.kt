package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.theme.AtelierTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SummaryFontScaleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun summary_atDoubleFontScale_neverBreaksAWordInTheMiddle() {
        setSummary(fontScale = 2f)

        val layouts = textLayouts()

        assertEquals(EXPECTED_TEXT_COUNT, layouts.size)
        layouts.forEach(::assertWrapsOnlyAtWhitespace)
    }

    @Test
    fun summary_atDefaultFontScale_keepsShortLabelsOnOneLine() {
        setSummary(fontScale = 1f)

        val layouts = textLayouts()

        assertEquals(EXPECTED_TEXT_COUNT, layouts.size)
        layouts.forEach(::assertWrapsOnlyAtWhitespace)
        SINGLE_LINE_TEXTS.forEach { text ->
            val matches = layouts.filter { it.layoutInput.text.text == text }
            assertTrue(text, matches.isNotEmpty())
            matches.forEach { assertEquals(text, 1, it.lineCount) }
        }
    }

    private fun assertWrapsOnlyAtWhitespace(layout: TextLayoutResult) {
        val text = layout.layoutInput.text.text
        for (line in 0 until layout.lineCount) {
            assertTrue(text, layout.getLineRight(line) <= layout.size.width)
        }
        for (line in 0 until layout.lineCount - 1) {
            assertTrue(text, text[layout.getLineEnd(line) - 1].isWhitespace())
        }
    }

    private fun setSummary(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                AtelierTheme {
                    Box(modifier = Modifier.width(CONTENT_WIDTH)) { SummaryUnderTest() }
                }
            }
        }
    }

    private fun textLayouts(): List<TextLayoutResult> =
        composeTestRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult))
            .fetchSemanticsNodes()
            .indices
            .map { index ->
                val results = mutableListOf<TextLayoutResult>()
                composeTestRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult))[index]
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                results.single()
            }

    private companion object {
        val CONTENT_WIDTH = 363.dp
        const val EXPECTED_TEXT_COUNT = 18
        val SINGLE_LINE_TEXTS = listOf(
            "Semana anterior",
            "Semana siguiente",
            "Regreso",
            "Topes",
            "Check-ins pendientes",
        )
    }
}

@Composable
private fun SummaryUnderTest() {
    Column {
        SummaryWeekNavigation(canGoNext = true, onIntent = {})
        ROWS.forEach { SummaryRow(row = it, onRowClick = {}) }
    }
}

private fun line(label: SummaryText, value: SummaryText) =
    SummaryRowState.Line(label = label, value = value, valueColor = Color.White)

private val ROWS = listOf(
    line(SummaryText(R.string.summary_relapses_label), SummaryText(R.string.summary_relapses_none)),
    line(
        SummaryText(R.string.summary_relapses_label),
        SummaryText(R.string.summary_relapses_open_other, listOf(3)),
    ),
    line(
        SummaryText(R.string.summary_relapses_history_label, listOf("sáb 24 oct")),
        SummaryText(R.string.summary_relapses_history_open),
    ),
    line(SummaryText(R.string.summary_limits_label), SummaryText(R.string.summary_limits_none)),
    line(SummaryText(R.string.summary_outings_pending), SummaryText.literal("0")),
    line(SummaryText(R.string.summary_money_debt_balance_today), SummaryText.literal("S/ 12,345.67")),
    line(
        SummaryText(R.string.summary_money_goal_label, listOf("Bicicleta de montaña")),
        SummaryText.literal("S/ 1,250.00 · 45%"),
    ),
    line(SummaryText(R.string.summary_money_not_spent), SummaryText.literal("S/ 120.00")),
)
