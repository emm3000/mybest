package com.emm.mybest.features.weight.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.core.datetime.longEs
import com.emm.mybest.domain.models.WeighInDirection
import com.emm.mybest.ui.components.atelier.Hairline
import kotlinx.datetime.DayOfWeek
import kotlin.math.abs

private val ROW_VERTICAL_PADDING = 14.dp
private const val KG_FORMAT = "%.1f"

@Composable
internal fun WeighInSummary(
    state: AddWeightState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TargetRow(targetKg = state.targetWeightKg, onOpenSettings = onOpenSettings)
        state.trend.latestKg?.let { LatestRow(it) }
        state.trend.direction?.let { ChangeRow(direction = it, changeKg = state.trend.changeSincePreviousKg) }
        state.trend.remainingToTargetKg?.let { RemainingRow(it) }
        WeighInDayRow(day = state.weighInDay, onOpenSettings = onOpenSettings)
    }
}

@Composable
private fun TargetRow(
    targetKg: Float?,
    onOpenSettings: () -> Unit,
) {
    if (targetKg == null) {
        PromptRow(stringResource(R.string.weigh_in_target_unset), onOpenSettings)
    } else {
        SummaryRow(stringResource(R.string.weigh_in_target_format, KG_FORMAT.format(targetKg)))
    }
}

@Composable
private fun LatestRow(latestKg: Float) {
    SummaryRow(stringResource(R.string.add_weight_last_record_format, KG_FORMAT.format(latestKg)))
}

@Composable
private fun ChangeRow(
    direction: WeighInDirection,
    changeKg: Float?,
) {
    val kg = KG_FORMAT.format(abs(changeKg ?: 0f))
    val text = when (direction) {
        WeighInDirection.DOWN -> stringResource(R.string.weigh_in_change_down_format, kg)
        WeighInDirection.UP -> stringResource(R.string.weigh_in_change_up_format, kg)
        WeighInDirection.UNCHANGED -> stringResource(R.string.weigh_in_change_none)
    }
    SummaryRow(text)
}

@Composable
private fun RemainingRow(remainingKg: Float) {
    SummaryRow(stringResource(R.string.weigh_in_remaining_format, KG_FORMAT.format(remainingKg)))
}

@Composable
private fun WeighInDayRow(
    day: DayOfWeek?,
    onOpenSettings: () -> Unit,
) {
    if (day == null) {
        PromptRow(stringResource(R.string.weigh_in_day_unset), onOpenSettings)
    } else {
        SummaryRow(stringResource(R.string.weigh_in_day_format, day.longEs().lowercase()))
    }
}

@Composable
private fun SummaryRow(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL_PADDING),
        style = MaterialTheme.typography.titleMedium,
    )
    Hairline()
}

@Composable
private fun PromptRow(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = ROW_VERTICAL_PADDING),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Hairline()
}
