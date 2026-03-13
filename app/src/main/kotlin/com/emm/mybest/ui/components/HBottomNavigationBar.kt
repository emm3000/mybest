package com.emm.mybest.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.core.navigation.Screen

sealed class HBottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelResId: Int,
) {
    data object Home : HBottomNavItem(Screen.Home, Icons.Default.Home, R.string.nav_home)
    data object History : HBottomNavItem(Screen.History, Icons.Default.History, R.string.nav_history)
    data object Insights : HBottomNavItem(Screen.Insights, Icons.Default.Insights, R.string.nav_insights)
    data object Timeline : HBottomNavItem(Screen.Timeline, Icons.Default.Timeline, R.string.nav_timeline)
}

@Composable
fun HBottomNavigationBar(
    currentRoute: Screen?,
    onNavItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val items = listOf(
        HBottomNavItem.Home,
        HBottomNavItem.History,
        HBottomNavItem.Insights,
        HBottomNavItem.Timeline,
    )

    NavigationBar(
        modifier = modifier,
        containerColor = cs.surface,
        contentColor = cs.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavItemClick(item.screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = cs.onPrimaryContainer,
                    selectedTextColor = cs.onPrimaryContainer,
                    unselectedIconColor = cs.onSurfaceVariant,
                    unselectedTextColor = cs.onSurfaceVariant,
                    indicatorColor = cs.primaryContainer,
                ),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.labelResId),
                    )
                },
                label = {
                    Text(text = stringResource(id = item.labelResId))
                },
            )
        }
    }
}
