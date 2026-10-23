package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

fun LocalTime.formatHhMm(): String = "%02d:%02d".format(hour, minute)
