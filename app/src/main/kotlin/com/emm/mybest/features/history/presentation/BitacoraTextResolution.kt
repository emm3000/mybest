package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R

@Composable
internal fun List<BitacoraTextPart>.resolve(): String {
    val separator = stringResource(R.string.bitacora_separator)
    return map { it.resolve() }.joinToString(separator)
}

@Suppress("SpreadOperator")
@Composable
private fun BitacoraTextPart.resolve(): String = when (this) {
    is BitacoraTextPart.Res -> stringResource(resId, *args.toTypedArray())
    is BitacoraTextPart.Plural -> pluralStringResource(resId, count, count)
    is BitacoraTextPart.Text -> value
}
