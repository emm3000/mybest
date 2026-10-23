package com.emm.mybest.features.capture.presentation

data class CapturedItemState(
    val id: Long,
    val appLabel: String,
    val title: String?,
    val text: String?,
    val postedAtDateLabel: String,
    val postedAtTimeLabel: String,
)
