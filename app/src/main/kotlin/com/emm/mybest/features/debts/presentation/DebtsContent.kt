package com.emm.mybest.features.debts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierTheme

private val CONTENT_GUTTER = 24.dp

@Composable
fun DebtsContent(
    state: DebtsState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoaded && state.debts.isEmpty()) {
            EmptyDebts(modifier = Modifier.weight(1f))
        } else {
            DebtList(debts = state.debts, onIntent = onIntent, modifier = Modifier.weight(1f))
        }
        HButton(
            text = stringResource(R.string.debts_add),
            onClick = { onIntent(DebtsIntent.OnAddClick) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(CONTENT_GUTTER),
        )
    }
}

@Composable
private fun EmptyDebts(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.debts_empty),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DebtList(
    debts: List<DebtItemState>,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(items = debts, key = { it.id }) { item ->
            DebtRow(item = item, onIntent = onIntent)
            Hairline()
        }
    }
}

private val PREVIEW_DEBTS = listOf(
    DebtItemState(2, "Letra moto", "S/ 2,150.00", "S/ 310.00", 5, isDueInstallmentPaid = true, isPaidOff = false),
    DebtItemState(
        1,
        "Tarjeta de crédito",
        "S/ 3,200.00",
        "S/ 420.00",
        15,
        isDueInstallmentPaid = false,
        isPaidOff = false,
    ),
)

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun DebtsContentPreview() {
    AtelierTheme {
        DebtsContent(state = DebtsState(isLoaded = true, debts = PREVIEW_DEBTS), onIntent = {})
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun DebtsContentEmptyPreview() {
    AtelierTheme {
        DebtsContent(state = DebtsState(isLoaded = true), onIntent = {})
    }
}
