package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HCard

internal fun getLabelForType(type: PhotoType): String = when (type) {
    PhotoType.FACE -> "Cara"
    PhotoType.ABDOMEN -> "Abdomen"
    PhotoType.BODY -> "Cuerpo"
    PhotoType.BREAKFAST -> "Desayuno"
    PhotoType.LUNCH -> "Almuerzo"
    PhotoType.DINNER -> "Cena"
    PhotoType.FOOD -> "Comida"
}

@Composable
internal fun HSourceOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        onClick = onClick,
        variant = CardVariant.Outlined,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
