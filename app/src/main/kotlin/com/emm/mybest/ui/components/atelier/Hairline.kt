package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierHairline

/**
 * 1dp hairline rule at 8% bone — the only divider used in Atelier Dark.
 *
 * @param inset optional horizontal inset so the rule sits inside the gutter
 *   (the design uses `inset = 28.dp` between adjacent plan rows).
 */
@Composable
fun Hairline(
    modifier: Modifier = Modifier,
    inset: Dp = 0.dp,
    color: Color = AtelierHairline,
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = inset)
            .height(1.dp)
            .background(color),
    )
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HairlinePreview() {
    Box(modifier = Modifier.background(AtelierBackground)) {
        Hairline()
    }
}
