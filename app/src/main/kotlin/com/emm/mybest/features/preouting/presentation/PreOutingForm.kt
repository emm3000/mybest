package com.emm.mybest.features.preouting.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.validation.ReturnLimitValidator
import kotlinx.datetime.LocalTime

private val DEFAULT_RETURN_TIME = LocalTime(2, 0)

data class PreOutingForm(
    val isOpen: Boolean = false,
    val moneyInput: String = "",
    val drinksInput: String = "",
    val returnTime: LocalTime? = null,
    val isTimePickerOpen: Boolean = false,
    val areCardsOff: Boolean = false,
    val isAccountLoaded: Boolean = false,
) {
    val moneyLimit: Money? get() = Money.parseOrNull(moneyInput)?.takeIf { it.cents > 0 }

    val pickerTime: LocalTime get() = returnTime ?: DEFAULT_RETURN_TIME

    val isReturnTimeOutOfRange: Boolean get() = returnTime?.let { !ReturnLimitValidator.isAllowed(it) } ?: false

    val canConfirm: Boolean
        get() = toLimits() != null && !isReturnTimeOutOfRange && areCardsOff && isAccountLoaded

    fun toLimits(): Limits? {
        val money = moneyLimit
        val drinks = drinksInput.toIntOrNull()
        val time = returnTime
        if (money == null || drinks == null || time == null) return null
        return Limits(money = money, drinks = drinks, returnTime = time)
    }
}
