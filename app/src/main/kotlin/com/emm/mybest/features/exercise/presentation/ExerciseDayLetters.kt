package com.emm.mybest.features.exercise.presentation

import com.emm.mybest.core.datetime.narrowEs
import kotlinx.datetime.DayOfWeek

internal fun DayOfWeek.atelierLetter(): String = narrowEs()
