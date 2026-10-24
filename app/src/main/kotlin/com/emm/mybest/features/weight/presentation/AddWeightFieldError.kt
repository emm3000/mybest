package com.emm.mybest.features.weight.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

enum class AddWeightFieldError {
    INVALID_FORMAT,
    OUT_OF_RANGE,
}

@StringRes
internal fun AddWeightFieldError.messageRes(): Int = when (this) {
    AddWeightFieldError.INVALID_FORMAT -> R.string.add_weight_error_invalid_format
    AddWeightFieldError.OUT_OF_RANGE -> R.string.add_weight_error_out_of_range
}
