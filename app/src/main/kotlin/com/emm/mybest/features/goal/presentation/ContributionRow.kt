package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierInkSecondary

private val ROW_VERTICAL = 14.dp
private val TEXT_GAP = 4.dp
private val ACTION_VERTICAL = 10.dp

@Composable
internal fun ContributionRow(
    item: ContributionItemState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TEXT_GAP)) {
            Text(text = item.amountLabel, style = MaterialTheme.typography.titleMedium)
            Text(text = item.dayLabel, style = MaterialTheme.typography.bodySmall, color = AtelierInkSecondary)
        }
        MicroLabel(
            text = stringResource(R.string.purchase_goal_contribution_delete),
            modifier = Modifier.clickable(onClick = onDelete).padding(vertical = ACTION_VERTICAL),
        )
    }
}
