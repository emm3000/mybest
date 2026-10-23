package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierTheme

private val SECTION_TOP = 14.dp
private val TEXT_GAP = 4.dp
private val AMOUNT_GAP = 16.dp
private val AMOUNT_SIZE = 28.sp

@Composable
internal fun NotSpentSection(
    amountLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = SECTION_TOP),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = AMOUNT_GAP),
                verticalArrangement = Arrangement.spacedBy(TEXT_GAP),
            ) {
                MicroLabel(text = stringResource(R.string.purchase_goal_not_spent_label))
                Text(
                    text = stringResource(R.string.purchase_goal_not_spent_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = AtelierInkSecondary,
                )
            }
            DisplayNumber(text = amountLabel, style = DisplayNumberStyle(fontSize = AMOUNT_SIZE, italic = true))
        }
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun NotSpentSectionPreview() {
    AtelierTheme {
        NotSpentSection(amountLabel = "S/ 380.00")
    }
}
