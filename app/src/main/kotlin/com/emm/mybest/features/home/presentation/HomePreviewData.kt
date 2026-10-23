package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress

private const val PREVIEW_GYM_COUNT = 3
private const val PREVIEW_GYM_TARGET = 4
private const val PREVIEW_ALCOHOL_COUNT = 1
private const val PREVIEW_ALCOHOL_LIMIT = 2

internal fun previewDailyChecks(): List<DailyCheckItem> = listOf(
    DailyCheckItem(DailyCheckType.GYM, isDone = true, progress = WeekProgress(PREVIEW_GYM_COUNT, PREVIEW_GYM_TARGET)),
    DailyCheckItem(DailyCheckType.DIET, isDone = false, progress = null),
    DailyCheckItem(DailyCheckType.ENGLISH, isDone = false, progress = null),
    DailyCheckItem(
        DailyCheckType.ALCOHOL,
        isDone = false,
        progress = WeekProgress(PREVIEW_ALCOHOL_COUNT, PREVIEW_ALCOHOL_LIMIT),
    ),
)
