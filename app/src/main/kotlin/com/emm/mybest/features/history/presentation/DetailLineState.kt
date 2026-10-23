package com.emm.mybest.features.history.presentation

data class DetailLineState(
    val label: String,
    val value: String,
    val tone: DetailLineTone = DetailLineTone.DEFAULT,
)
