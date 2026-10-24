package com.emm.mybest.ui.components

internal data class TimePickerCellModel(
    val value: Int,
    val isSelected: Boolean,
    val distanceFromSelection: Int,
    val contentDescription: String,
)
