package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R

@Composable
internal fun List<LogbookTextPart>.resolve(): String {
    val separator = stringResource(R.string.logbook_separator)
    return map { it.resolve() }.joinToString(separator)
}

@Suppress("SpreadOperator")
@Composable
internal fun LogbookTextPart.resolve(): String = when (this) {
    is LogbookTextPart.Res -> stringResource(resId, *args.toTypedArray())
    is LogbookTextPart.Plural -> pluralStringResource(resId, count, count)
    is LogbookTextPart.Text -> value
}
