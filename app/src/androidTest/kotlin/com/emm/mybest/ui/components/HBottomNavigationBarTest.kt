package com.emm.mybest.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.core.navigation.Screen
import com.emm.mybest.ui.theme.AtelierTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HBottomNavigationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hBottomNavigationBar_atDoubleFontScale_sharesOneLabelSizeThatFits() {
        setBar(fontScale = 2f)

        val layouts = LABELS.map(::layoutOf)

        val fontSizes = layouts.map { it.layoutInput.style.fontSize }.distinct()
        assertEquals(1, fontSizes.size)
        assertTrue(fontSizes.single() < LABEL_SMALL_FONT_SIZE)
        assertEquals(1, layouts.map { it.size.height }.distinct().size)
        layouts.forEach { layout ->
            assertFalse(layout.didOverflowWidth)
            assertEquals(1, layout.lineCount)
            assertTrue(layout.multiParagraph.maxIntrinsicWidth <= layout.size.width)
        }
    }

    @Test
    fun hBottomNavigationBar_atDefaultFontScale_keepsLabelSmallSize() {
        setBar(fontScale = 1f)

        LABELS.map(::layoutOf).forEach { layout ->
            assertEquals(LABEL_SMALL_FONT_SIZE, layout.layoutInput.style.fontSize)
        }
    }

    private fun setBar(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                AtelierTheme {
                    Box(modifier = Modifier.width(BAR_WIDTH)) {
                        HBottomNavigationBar(currentRoute = Screen.Home, onNavItemClick = {})
                    }
                }
            }
        }
    }

    private fun layoutOf(label: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        composeTestRule.onNodeWithText(label).performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
            it(results)
        }
        return results.single()
    }

    private companion object {
        val BAR_WIDTH = 411.dp
        val LABEL_SMALL_FONT_SIZE = 10.sp
        val LABELS = listOf("DIARIO", "RESUMEN", "HISTORIA", "AJUSTES")
    }
}
