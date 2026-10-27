package com.emm.mybest.features.debts.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val ROW_GUTTER = 24.dp
private val ROW_VERTICAL = 14.dp
private val ACTIONS_GAP = 20.dp
private val ACTION_VERTICAL = 10.dp
private val TEXT_GAP = 4.dp

@Composable
internal fun DebtRow(
    item: DebtItemState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_GUTTER, vertical = ROW_VERTICAL),
    ) {
        DebtSummaryLine(item = item)
        DebtActions(item = item, onIntent = onIntent)
    }
}

@Composable
private fun DebtSummaryLine(item: DebtItemState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TEXT_GAP)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    R.string.debts_row_details,
                    item.installmentLabel,
                    item.paymentDay,
                    item.balanceLabel,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.isDueInstallmentPaid) {
            MicroLabel(
                text = stringResource(R.string.debts_installment_paid),
                style = MicroLabelStyle(tone = MicroLabelTone.Done),
            )
        }
    }
}

@Composable
private fun DebtActions(
    item: DebtItemState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.padding(top = ACTION_VERTICAL),
        horizontalArrangement = Arrangement.spacedBy(ACTIONS_GAP),
    ) {
        PayAction(item = item, onIntent = onIntent)
        RowAction(text = stringResource(R.string.debts_edit), onClick = { onIntent(DebtsIntent.OnEditClick(item.id)) })
        RowAction(
            text = stringResource(R.string.debts_delete),
            onClick = { onIntent(DebtsIntent.OnDeleteClick(item.id)) },
        )
    }
}

@Composable
private fun PayAction(
    item: DebtItemState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        item.isPaidOff -> DoneTag(text = stringResource(R.string.debts_paid_off), modifier = modifier)
        item.isDueInstallmentPaid -> DoneTag(text = stringResource(R.string.debts_paid), modifier = modifier)
        else -> RowAction(
            text = stringResource(R.string.debts_mark_paid),
            onClick = { onIntent(DebtsIntent.OnPayClick(item.id)) },
            modifier = modifier,
        )
    }
}

@Composable
private fun DoneTag(text: String, modifier: Modifier = Modifier) {
    MicroLabel(
        text = text,
        style = MicroLabelStyle(tone = MicroLabelTone.Done, isSingleLine = true),
        modifier = modifier.padding(vertical = ACTION_VERTICAL),
    )
}

@Composable
private fun RowAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MicroLabel(
        text = text,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = ACTION_VERTICAL),
        style = MicroLabelStyle(isSingleLine = true),
    )
}
