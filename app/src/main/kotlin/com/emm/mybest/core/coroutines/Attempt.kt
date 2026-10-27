package com.emm.mybest.core.coroutines

import kotlinx.coroutines.CancellationException

/**
 * Runs [block] and returns its value, or the failure it threw. A [CancellationException] is rethrown and never
 * returned, so cancellation keeps propagating.
 */
suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
