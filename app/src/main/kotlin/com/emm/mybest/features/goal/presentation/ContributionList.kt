package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierInkSecondary

private val LABEL_BOTTOM = 4.dp
private val EMPTY_TOP = 14.dp

@Composable
internal fun ContributionList(
    contributions: List<ContributionItemState>,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MicroLabel(
            text = stringResource(R.string.purchase_goal_contributions_label),
            modifier = Modifier.padding(bottom = LABEL_BOTTOM),
        )
        Hairline()
        if (contributions.isEmpty()) {
            Text(
                text = stringResource(R.string.purchase_goal_contributions_empty),
                modifier = Modifier.padding(top = EMPTY_TOP),
                style = MaterialTheme.typography.bodyMedium,
                color = AtelierInkSecondary,
            )
        }
        contributions.forEach { item ->
            ContributionRow(item = item, onDelete = { onIntent(PurchaseGoalIntent.OnContributionDelete(item.id)) })
            Hairline()
        }
    }
}
