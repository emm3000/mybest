package com.emm.mybest.features.weight.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

private val FIELDS_BOTTOM_GAP = 24.dp

@Composable
fun AddWeightScreen(
    viewModel: AddWeightViewModel,
    onBackClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBackClick by rememberUpdatedState(onBackClick)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AddWeightEffect.NavigateBack -> currentOnBackClick()
                AddWeightEffect.OpenSettings -> currentOnOpenSettings()
                is AddWeightEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    AddWeightContent(
        state = state,
        snackbarHostState = snackbarHostState,
        callbacks = AddWeightCallbacks(onBackClick = currentOnBackClick, onIntent = viewModel::onIntent),
        modifier = modifier,
    )
}

@Composable
private fun AddWeightContent(
    state: AddWeightState,
    snackbarHostState: SnackbarHostState,
    callbacks: AddWeightCallbacks,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        topBar = {
            AtelierAppBar(
                title = stringResource(R.string.add_weight_app_bar_title),
                onBack = callbacks.onBackClick,
            )
        },
    ) { padding ->
        AddWeightBody(state = state, padding = padding, onIntent = callbacks.onIntent)
    }
}

@Composable
private fun AddWeightBody(
    state: AddWeightState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onIntent: (AddWeightIntent) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .padding(24.dp)
            .fillMaxSize()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            AddWeightFields(state = state, onIntent = onIntent, modifier = Modifier.padding(bottom = FIELDS_BOTTOM_GAP))
            HButton(
                text = stringResource(R.string.add_weight_save_button),
                onClick = { onIntent(AddWeightIntent.OnSaveClick) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.weight.isNotBlank() && state.weightError == null && !state.isLoading,
                isLoading = state.isLoading,
            )
        }
    }
}

@Composable
private fun AddWeightFields(
    state: AddWeightState,
    onIntent: (AddWeightIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        WeighInSummary(state = state, onOpenSettings = { onIntent(AddWeightIntent.OnOpenSettingsClick) })
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
            errorMessage = state.weightError?.let { stringResource(it.messageRes()) },
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
                trend = WeighInTrend(latestKg = 81.2f, changeSincePreviousKg = -0.6f, remainingToTargetKg = 3.2f),
                targetWeightKg = 78f,
                weighInDay = DayOfWeek.MONDAY,
                weighInDayLabel = "lunes",
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            callbacks = AddWeightCallbacks(onBackClick = {}, onIntent = {}),
        )
    }
}
