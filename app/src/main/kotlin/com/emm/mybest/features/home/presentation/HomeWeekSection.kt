package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierHairline
import com.emm.mybest.ui.theme.AtelierTheme

private const val DAYS_IN_WEEK = 7

private val SECTION_PADDING_HORIZONTAL = 28.dp
private val SECTION_PADDING_VERTICAL = 14.dp
private val WEEK_ROW_HEIGHT_MIN = 48.dp
private val DEFINE_TARGET_PADDING_START = 14.dp
private val WEEK_BAR_SEGMENT_HEIGHT = 4.dp
private val WEEK_BAR_SEGMENT_WIDTH = 14.dp
private val WEEK_BAR_GAP = 3.dp
private val WEEK_COUNT_WIDTH = 104.dp

@Composable
internal fun HomeWeekSection(
    checks: List<DailyCheckItem>,
    onDefineTarget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = SECTION_PADDING_HORIZONTAL)) {
        MicroLabel(
            text = stringResource(R.string.home_week_title).uppercase(),
            modifier = Modifier.padding(vertical = SECTION_PADDING_VERTICAL),
        )
        checks.forEach { item ->
            Hairline()
            WeekProgressRow(item = item, onDefineTarget = onDefineTarget)
        }
    }
}

@Composable
private fun WeekProgressRow(item: DailyCheckItem, onDefineTarget: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = WEEK_ROW_HEIGHT_MIN),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(weekLabelRes(item.type)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val progress = item.progress
        if (progress == null) {
            DefineTargetAction(onDefineTarget)
        } else {
            WeekProgressValue(type = item.type, progress = progress)
        }
    }
}

@Composable
private fun DefineTargetAction(onDefineTarget: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = WEEK_ROW_HEIGHT_MIN)
            .clickable(role = Role.Button, onClick = onDefineTarget)
            .padding(start = DEFINE_TARGET_PADDING_START),
        contentAlignment = Alignment.CenterEnd,
    ) {
        MicroLabel(
            text = stringResource(R.string.home_define_target).uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Warm),
        )
    }
}

@Composable
private fun WeekProgressValue(type: DailyCheckType, progress: WeekProgress) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (type != DailyCheckType.ALCOHOL) {
            WeekBar(count = progress.count)
        }
        Text(
            text = weekProgressText(type, progress),
            modifier = Modifier.width(WEEK_COUNT_WIDTH).padding(start = DEFINE_TARGET_PADDING_START),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun weekProgressText(type: DailyCheckType, progress: WeekProgress): String {
    val format = if (type == DailyCheckType.ALCOHOL) {
        R.string.home_week_alcohol_progress_format
    } else {
        R.string.home_week_progress_format
    }
    return stringResource(format, progress.count, progress.target)
}

@Composable
private fun WeekBar(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(WEEK_BAR_GAP)) {
        repeat(DAYS_IN_WEEK) { index ->
            Box(
                modifier = Modifier
                    .width(WEEK_BAR_SEGMENT_WIDTH)
                    .height(WEEK_BAR_SEGMENT_HEIGHT)
                    .background(if (index < count) AtelierDone else AtelierHairline),
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HomeWeekSectionPreview() {
    AtelierTheme {
        HomeWeekSection(checks = previewDailyChecks(), onDefineTarget = {})
    }
}
