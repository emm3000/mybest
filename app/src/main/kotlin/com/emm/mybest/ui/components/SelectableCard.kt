package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

@Composable
fun HSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    HCard(
        modifier = modifier.semantics {
            this.selected = selected
            stateDescription = if (selected) "Seleccionado" else "No seleccionado"
        },
        variant = if (selected) CardVariant.Filled else CardVariant.Outlined,
        onClick = onClick,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@PreviewLightDark
@Composable
private fun HSelectableCardPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HSelectableCard(
                    selected = true,
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                ) {
                    Text(text = "Selected Card")
                }

                HSelectableCard(
                    selected = false,
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                ) {
                    Text(text = "Unselected Card")
                }
            }
        }
    }
}
