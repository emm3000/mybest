package com.emm.mybest.features.home.presentation

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme

private val INSTALLMENT_PADDING_VERTICAL = 16.dp
private val INSTALLMENT_LABEL_GAP = 6.dp
private val INSTALLMENT_AMOUNT_SIZE = 24.sp

@Composable
internal fun HomeInstallmentRow(
    installment: HomeInstallment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline(inset = HOME_SECTION_PADDING_HORIZONTAL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = HOME_SECTION_PADDING_HORIZONTAL, vertical = INSTALLMENT_PADDING_VERTICAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(INSTALLMENT_LABEL_GAP)) {
                MicroLabel(text = stringResource(R.string.home_next_installment_format, installment.dateLabel))
                Text(text = installment.debtName, style = MaterialTheme.typography.titleMedium)
            }
            DisplayNumber(
                text = installment.amountLabel,
                style = DisplayNumberStyle(fontSize = INSTALLMENT_AMOUNT_SIZE, italic = true),
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HomeInstallmentRowPreview() {
    AtelierTheme {
        HomeInstallmentRow(
            installment = HomeInstallment("15 NOV", "Tarjeta de crédito", "S/ 420.00"),
            onClick = {},
        )
    }
}
