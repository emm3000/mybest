package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

/**
 * Copy the screen resolves with `stringResource`: the [res] string filled with [args].
 */
data class SummaryText(
    @StringRes val res: Int,
    val args: List<Any> = emptyList(),
) {
    companion object {
        fun literal(text: String): SummaryText = SummaryText(R.string.summary_literal, listOf(text))
    }
}
