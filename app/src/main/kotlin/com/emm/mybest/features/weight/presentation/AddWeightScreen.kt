package com.emm.mybest.features.weight.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddWeightScreen(
    viewModel: AddWeightViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AddWeightEffect.NavigateBack -> currentOnBackClick()
                is AddWeightEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    AddWeightContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = currentOnBackClick,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun AddWeightContent(
    state: AddWeightState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onIntent: (AddWeightIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        topBar = { AtelierAppBar(title = stringResource(R.string.add_weight_app_bar_title), onBack = onBackClick) },
    ) { padding ->
        AddWeightBody(state = state, padding = padding, onIntent = onIntent)
    }
}

@Composable
private fun AddWeightBody(
    state: AddWeightState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onIntent: (AddWeightIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(24.dp)
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = stringResource(R.string.add_weight_field_label),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        HInput(
            value = state.weight,
            onValueChange = { onIntent(AddWeightIntent.OnWeightChange(it)) },
            label = stringResource(R.string.add_weight_kg_label),
            placeholder = stringResource(R.string.add_weight_kg_placeholder),
            supportingText = state.lastRecordedWeight?.let {
                stringResource(R.string.add_weight_last_record_format, "%.1f".format(it))
            },
            errorMessage = state.weightError,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Rounded.Scale, contentDescription = null) },
            singleLine = true,
        )
        HInput(
            value = state.note,
            onValueChange = { onIntent(AddWeightIntent.OnNoteChange(it)) },
            label = stringResource(R.string.add_weight_note_label),
            placeholder = stringResource(R.string.add_weight_note_placeholder),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
        )
        Spacer(modifier = Modifier.weight(1f))
        HButton(
            text = stringResource(R.string.add_weight_save_button),
            onClick = { onIntent(AddWeightIntent.OnSaveClick) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.weight.isNotBlank() && state.weightError == null && !state.isLoading,
            isLoading = state.isLoading,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWeightScreenPreview() {
    AtelierTheme {
        AddWeightContent(
            state = AddWeightState(
                weight = "80.5",
                note = "Después del entrenamiento",
                lastRecordedWeight = 81.2f,
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onIntent = {},
        )
    }
}
