package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

@Composable
fun HMediaCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    aspectRatio: Float = 1f,
    cornerRadius: Dp = 12.dp,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    media: @Composable BoxScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val border = BorderStroke(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) cs.primary else cs.outlineVariant,
    )
    val shape = RoundedCornerShape(cornerRadius)

    HCard(
        modifier = modifier,
        variant = CardVariant.Outlined,
        onClick = onClick,
        border = border,
        cornerRadius = cornerRadius,
        containerColor = cs.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(shape),
        ) {
            media()
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.primary.copy(alpha = 0.14f)),
                )
            }
        }

        if (footer != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = footer,
            )
        }
    }
}

@Composable
fun HMediaOverlayLabel(
    text: String,
    modifier: Modifier = Modifier,
    align: Alignment = Alignment.BottomStart,
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .align(align)
                .background(
                    color = cs.scrim.copy(alpha = 0.65f),
                    shape = when (align) {
                        Alignment.BottomStart -> RoundedCornerShape(topEnd = 10.dp)
                        Alignment.BottomEnd -> RoundedCornerShape(topStart = 10.dp)
                        Alignment.TopStart -> RoundedCornerShape(bottomEnd = 10.dp)
                        Alignment.TopEnd -> RoundedCornerShape(bottomStart = 10.dp)
                        else -> RoundedCornerShape(10.dp)
                    },
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = text,
                color = cs.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HMediaCardPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                HMediaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    selected = true,
                    aspectRatio = 16f / 9f,
                    media = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        )
                        HMediaOverlayLabel(
                            text = "12/03/26",
                            align = Alignment.BottomStart,
                        )
                        HMediaOverlayLabel(
                            text = "ANTES",
                            align = Alignment.TopEnd,
                        )
                    },
                    footer = {
                        Text(
                            text = "Tipo: Cuerpo",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                HMediaCard(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(120.dp),
                    selected = false,
                    media = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                        )
                        HMediaOverlayLabel(
                            text = "Actual",
                            align = Alignment.BottomStart,
                        )
                    },
                )
            }
        }
    }
}
