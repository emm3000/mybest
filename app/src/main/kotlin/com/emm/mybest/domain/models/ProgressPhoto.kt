package com.emm.mybest.domain.models

import java.time.LocalDate

data class ProgressPhoto(
    val id: String,
    val habitRecordId: String? = null,
    val date: LocalDate,
    val type: String, // Use String or create a domain enum if needed, for simplicity let's use the data type if available
    val photoPath: String
)
