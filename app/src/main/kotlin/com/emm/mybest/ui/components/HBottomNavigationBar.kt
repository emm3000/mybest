package com.emm.mybest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.core.navigation.Screen

private val NAV_BAR_HEIGHT = 60.dp
private val ACTIVE_INDICATOR_WIDTH = 26.dp
private val ACTIVE_INDICATOR_HEIGHT = 1.dp
private val ICON_SIZE = 18.dp
private val LABEL_GAP = 7.dp
private val ITEM_TOP_PADDING = 10.dp
private val ITEM_BOTTOM_PADDING = 12.dp

private data class AtelierNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelResId: Int,
)

private val NAV_ITEMS = listOf(
    AtelierNavItem(Screen.Home, Icons.AutoMirrored.Outlined.MenuBook, R.string.nav_home),
    AtelierNavItem(Screen.Insights, Icons.AutoMirrored.Outlined.TrendingUp, R.string.nav_insights),
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
    Column(modifier = modifier.fillMaxWidth().background(cs.background)) {
        HSeparator(color = cs.outlineVariant)
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = NAV_BAR_HEIGHT)) {
            NAV_ITEMS.forEach { item ->
                AtelierNavCell(
                    item = item,
                    isActive = currentRoute == item.screen,
                    onClick = { onNavItemClick(item.screen) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AtelierNavCell(
    item: AtelierNavItem,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val tone = if (isActive) cs.onBackground else cs.outline
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
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(ACTIVE_INDICATOR_WIDTH)
                    .height(ACTIVE_INDICATOR_HEIGHT)
                    .background(cs.onBackground),
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(id = item.labelResId),
                tint = tone,
                modifier = Modifier.size(ICON_SIZE),
            )
            Spacer(modifier = Modifier.height(LABEL_GAP))
            Text(
                text = stringResource(id = item.labelResId).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = tone,
            )
        }
    }
}
