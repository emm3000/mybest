package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.emm.mybest.ui.components.atelier.AtelierCaret
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierTheme

@Composable
internal fun HomeShortcutsSection(
    shortcuts: List<HomeShortcut>,
    onShortcutClick: (HomeShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = HOME_SECTION_PADDING_HORIZONTAL)) {
        shortcuts.forEach { shortcut ->
            Hairline()
            ShortcutRow(shortcut = shortcut, onClick = { onShortcutClick(shortcut) })
        }
    }
}

@Composable
private fun ShortcutRow(shortcut: HomeShortcut, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HOME_ROW_HEIGHT_MIN)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(shortcutLabelRes(shortcut)), style = MaterialTheme.typography.bodyLarge)
        AtelierCaret()
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HomeShortcutsSectionPreview() {
    AtelierTheme {
        HomeShortcutsSection(shortcuts = HomeShortcut.entries, onShortcutClick = {})
    }
}
