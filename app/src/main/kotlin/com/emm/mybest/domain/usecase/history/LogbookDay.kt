package com.emm.mybest.domain.usecase.history

import kotlinx.datetime.LocalDate

/**
 * The [entries] recorded on [appDay], in the fixed order the Logbook shows them.
 */
data class LogbookDay(
    val appDay: LocalDate,
    val entries: List<LogbookEntry>,
)
