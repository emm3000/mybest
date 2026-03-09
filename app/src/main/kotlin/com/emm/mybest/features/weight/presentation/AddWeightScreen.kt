package com.emm.mybest.features.weight.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.theme.MyBestTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddWeightScreen(
    viewModel: AddWeightViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HTopBar(
                title = "Registrar Peso",
                navigationIcon = {
                    HIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = onBackClick,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "¿Cuánto pesas hoy?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            HInput(
                value = state.weight,
                onValueChange = { onIntent(AddWeightIntent.OnWeightChange(it)) },
                label = "Peso (kg)",
                placeholder = "Ej: 72.4",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Rounded.Scale, contentDescription = null) },
                singleLine = true,
            )

            HInput(
                value = state.note,
                onValueChange = { onIntent(AddWeightIntent.OnNoteChange(it)) },
                label = "Nota (opcional)",
                placeholder = "Ej: Después del entrenamiento",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 3,
            )

            Spacer(modifier = Modifier.weight(1f))

            HButton(
                text = "Guardar Registro",
                onClick = { onIntent(AddWeightIntent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.weight.isNotEmpty() && !state.isLoading,
                isLoading = state.isLoading,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWeightScreenPreview() {
    MyBestTheme {
        AddWeightContent(
            state = AddWeightState(
                weight = "80.5",
                note = "Después del entrenamiento",
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onIntent = {},
        )
    }
}
