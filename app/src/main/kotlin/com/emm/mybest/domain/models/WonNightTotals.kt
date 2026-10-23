package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The sum of the estimated costs of these won nights; a won night without an estimate counts as zero. It is never real
 * money.
 */
fun List<WonNight>.estimatedTotal(): Money =
    fold(Money.ZERO) { total, night -> total + (night.estimatedCost ?: Money.ZERO) }
