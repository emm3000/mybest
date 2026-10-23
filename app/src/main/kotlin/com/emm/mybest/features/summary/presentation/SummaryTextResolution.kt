package com.emm.mybest.features.summary.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun SummaryText.resolve(): String = stringResource(res, formatArgs = args.toTypedArray())
