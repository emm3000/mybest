package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.theme.AtelierTheme

private val BUTTON_MIN_HEIGHT = 56.dp

@Composable
fun CheckInEmptyContent(
    onIntent: (CheckInIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckInPage(modifier = modifier, footer = { EmptyFooter(onIntent = onIntent) }) {
        Text(text = stringResource(R.string.check_in_empty_headline), style = MaterialTheme.typography.headlineLarge)
        Text(
            text = stringResource(R.string.check_in_empty_caption),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColumnScope.EmptyFooter(onIntent: (CheckInIntent) -> Unit) {
    HButton(
        onClick = { onIntent(CheckInIntent.OnDoneClick) },
        modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
    ) {
        Text(text = stringResource(R.string.check_in_empty_back))
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInEmptyContentPreview() {
    AtelierTheme {
        CheckInEmptyContent(onIntent = {})
    }
}
