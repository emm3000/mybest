package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.emm.mybest.R
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierWarm

@Composable
internal fun HomeRoutedScreen(
    destination: HomeDestination,
    onShowDailyChecks: () -> Unit,
    slots: HomeSlots,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = slots.bottomBar,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)) {
            Box(modifier = Modifier.weight(1f)) { slots.routedContent(destination, onShowDailyChecks) }
            Hairline()
            ShowDailyChecksLink(onClick = onShowDailyChecks)
        }
    }
}

@Composable
private fun ShowDailyChecksLink(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HOME_ROW_HEIGHT_MIN)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HOME_SECTION_PADDING_HORIZONTAL),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.home_show_daily_checks),
            style = MaterialTheme.typography.bodyMedium,
            color = AtelierWarm,
        )
    }
}
