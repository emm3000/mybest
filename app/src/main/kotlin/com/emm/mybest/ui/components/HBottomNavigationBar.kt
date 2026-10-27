package com.emm.mybest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.core.navigation.Screen
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierHairline

private val NAV_BAR_HEIGHT = 60.dp
private val ICON_SIZE = 18.dp
private val LABEL_GAP = 7.dp
private val ITEM_TOP_PADDING = 10.dp
private val ITEM_BOTTOM_PADDING = 12.dp
private val LABEL_MIN_FONT_SIZE = 6.sp
private val LABEL_FONT_STEP = 0.25.sp
private val LABEL_SIDE_PADDING = 4.dp

private data class AtelierNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelResId: Int,
)

private data class AtelierNavCellModel(
    val item: AtelierNavItem,
    val label: String,
    val isActive: Boolean,
    val labelFontSize: TextUnit,
)

private val NAV_ITEMS = listOf(
    AtelierNavItem(Screen.Home, Icons.AutoMirrored.Outlined.MenuBook, R.string.nav_home),
    AtelierNavItem(Screen.WeeklySummary, Icons.AutoMirrored.Outlined.TrendingUp, R.string.nav_summary),
    AtelierNavItem(Screen.History, Icons.Outlined.History, R.string.nav_history),
    AtelierNavItem(Screen.Settings, Icons.Outlined.Settings, R.string.nav_settings),
)

@Composable
fun HBottomNavigationBar(
    currentRoute: Screen?,
    onNavItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Hairline(color = AtelierHairline)
        AtelierNavRow(currentRoute = currentRoute, onNavItemClick = onNavItemClick)
    }
}

@Composable
private fun AtelierNavRow(
    currentRoute: Screen?,
    onNavItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = NAV_ITEMS.map { stringResource(id = it.labelResId).uppercase() }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val labelMaxWidthPx = constraints.maxWidth / NAV_ITEMS.size - with(LocalDensity.current) {
            LABEL_SIDE_PADDING.roundToPx() * 2
        }
        val labelFontSize = rememberSharedLabelFontSize(labels, labelMaxWidthPx)
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = NAV_BAR_HEIGHT)) {
            NAV_ITEMS.forEachIndexed { index, item ->
                AtelierNavCell(
                    model = AtelierNavCellModel(item, labels[index], currentRoute == item.screen, labelFontSize),
                    onClick = { onNavItemClick(item.screen) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun rememberSharedLabelFontSize(labels: List<String>, labelMaxWidthPx: Int): TextUnit {
    val style = MaterialTheme.typography.labelSmall
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer(cacheSize = labels.size)
    return remember(labels, labelMaxWidthPx, style, density) {
        generateSequence(style.fontSize.value) { it - LABEL_FONT_STEP.value }
            .takeWhile { it >= LABEL_MIN_FONT_SIZE.value }
            .map { it.sp }
            .firstOrNull { size ->
                labels.all { label ->
                    textMeasurer.measure(label, style.copy(fontSize = size), softWrap = false).size.width <= labelMaxWidthPx
                }
            } ?: LABEL_MIN_FONT_SIZE
    }
}

@Composable
private fun AtelierNavCell(
    model: AtelierNavCellModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val tone = if (model.isActive) cs.onBackground else cs.outline
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(top = ITEM_TOP_PADDING, bottom = ITEM_BOTTOM_PADDING),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = model.item.icon,
                contentDescription = stringResource(id = model.item.labelResId),
                tint = tone,
                modifier = Modifier.size(ICON_SIZE),
            )
            Spacer(modifier = Modifier.height(LABEL_GAP))
            NavLabel(text = model.label, color = tone, fontSize = model.labelFontSize)
        }
    }
}

@Composable
private fun NavLabel(text: String, color: Color, fontSize: TextUnit, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LABEL_SIDE_PADDING),
        style = MaterialTheme.typography.labelSmall.copy(
            color = color,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
        ),
        softWrap = false,
        maxLines = 1,
    )
}
