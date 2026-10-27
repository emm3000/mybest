package com.emm.mybest.domain.validation

import com.emm.mybest.core.datetime.APP_DAY_START
import com.emm.mybest.core.datetime.EVENING_START
import kotlinx.datetime.LocalTime

/**
 * Rules for the return-time limit of an outing: it must fall in the evening or the small hours, never in the daytime
 * between [APP_DAY_START] and [EVENING_START].
 */
object ReturnLimitValidator {

    fun isAllowed(returnTime: LocalTime): Boolean = returnTime < APP_DAY_START || returnTime >= EVENING_START
}
