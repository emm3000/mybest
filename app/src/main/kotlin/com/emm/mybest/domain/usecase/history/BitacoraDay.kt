package com.emm.mybest.domain.usecase.history

import kotlinx.datetime.LocalDate

/**
 * The [entries] recorded on [appDay], in the fixed order the Bitácora shows them.
 */
data class BitacoraDay(
    val appDay: LocalDate,
    val entries: List<BitacoraEntry>,
)
