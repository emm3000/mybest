package com.emm.mybest.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emm.mybest.viewmodel.AddWeightEffect
import com.emm.mybest.viewmodel.AddWeightIntent
import com.emm.mybest.viewmodel.AddWeightViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightScreen(
    viewModel: AddWeightViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AddWeightEffect.NavigateBack -> onBackClick()
                is AddWeightEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registrar Peso") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "¿Cuánto pesas hoy?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = state.weight,
                onValueChange = { viewModel.onIntent(AddWeightIntent.OnWeightChange(it)) },
                label = { Text("Peso (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Rounded.Scale, null) },
                singleLine = true
            )
            
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onIntent(AddWeightIntent.OnNoteChange(it)) },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.onIntent(AddWeightIntent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = state.weight.isNotEmpty() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Registro", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
