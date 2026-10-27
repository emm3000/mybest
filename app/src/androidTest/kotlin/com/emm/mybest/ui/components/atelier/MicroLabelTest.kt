package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val LABEL = "MARCAR CUOTA PAGADA"

class MicroLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun lineCountFor(style: MicroLabelStyle): Int {
        composeTestRule.setContent {
            AtelierTheme {
                Box(modifier = Modifier.width(80.dp)) {
                    MicroLabel(text = LABEL, style = style)
                }
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        composeTestRule.onNodeWithText(LABEL).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return layouts.first().lineCount
    }

    @Test
    fun singleLineStyleKeepsTheLabelOnOneLine() {
        assertEquals(1, lineCountFor(MicroLabelStyle(isSingleLine = true)))
    }

    @Test
    fun defaultStyleWrapsTheLabelWhenTheWidthIsShort() {
        assertTrue(lineCountFor(MicroLabelStyle()) > 1)
    }
}
