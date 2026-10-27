package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Free text the user writes in the Bitácora, stamped with the moment it was written ([createdAt])
 * and the app day it belongs to ([appDay]).
 */
data class Note(
    val id: Long,
    val appDay: LocalDate,
    val createdAt: Instant,
    val text: String,
)
