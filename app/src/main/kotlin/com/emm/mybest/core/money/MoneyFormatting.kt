package com.emm.mybest.core.money

import java.util.Locale

private const val PEN_SYMBOL = "S/"

/**
 * Formats this amount as soles, e.g. `S/ 1,234.50`; negative amounts read `-S/ 5.00`.
 */
fun Money.formatPen(): String {
    val absoluteCents = kotlin.math.abs(cents)
    val soles = absoluteCents / Money.CENTS_PER_SOL
    val remainderCents = absoluteCents % Money.CENTS_PER_SOL
    val sign = if (isNegative) "-" else ""
    return String.format(Locale.ROOT, "%s%s %,d.%02d", sign, PEN_SYMBOL, soles, remainderCents)
}
