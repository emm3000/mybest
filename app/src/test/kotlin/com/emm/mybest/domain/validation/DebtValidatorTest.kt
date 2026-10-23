package com.emm.mybest.domain.validation

import com.emm.mybest.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

private val VALID = DebtDraft(
    name = "Tarjeta",
    balance = Money(320_000),
    installment = Money(42_000),
    paymentDay = 15,
)

class DebtValidatorTest {

    @Test
    fun `a complete draft has no errors`() {
        assertEquals(emptyList<DebtValidationError>(), DebtValidator.validate(VALID))
    }

    @Test
    fun `a blank name is rejected`() {
        assertEquals(listOf(DebtValidationError.BlankName), DebtValidator.validate(VALID.copy(name = "  ")))
    }

    @Test
    fun `a missing balance is rejected`() {
        assertEquals(listOf(DebtValidationError.MissingBalance), DebtValidator.validate(VALID.copy(balance = null)))
    }

    @Test
    fun `a negative balance is rejected`() {
        val errors = DebtValidator.validate(VALID.copy(balance = Money(-1)))

        assertEquals(listOf(DebtValidationError.NegativeBalance), errors)
    }

    @Test
    fun `a zero balance is accepted`() {
        assertEquals(emptyList<DebtValidationError>(), DebtValidator.validate(VALID.copy(balance = Money.ZERO)))
    }

    @Test
    fun `a zero or missing installment is rejected`() {
        val expected = listOf(DebtValidationError.NonPositiveInstallment)

        assertEquals(expected, DebtValidator.validate(VALID.copy(installment = Money.ZERO)))
        assertEquals(expected, DebtValidator.validate(VALID.copy(installment = null)))
    }

    @Test
    fun `a payment day outside 1 to 31 is rejected`() {
        val expected = listOf(DebtValidationError.PaymentDayOutOfRange)

        assertEquals(expected, DebtValidator.validate(VALID.copy(paymentDay = 0)))
        assertEquals(expected, DebtValidator.validate(VALID.copy(paymentDay = 32)))
        assertEquals(expected, DebtValidator.validate(VALID.copy(paymentDay = null)))
    }

    @Test
    fun `the first and last payment days are accepted`() {
        assertEquals(emptyList<DebtValidationError>(), DebtValidator.validate(VALID.copy(paymentDay = 1)))
        assertEquals(emptyList<DebtValidationError>(), DebtValidator.validate(VALID.copy(paymentDay = 31)))
    }

    @Test
    fun `every broken field reports its own error`() {
        val draft = DebtDraft(name = "", balance = null, installment = null, paymentDay = null)

        assertEquals(
            listOf(
                DebtValidationError.BlankName,
                DebtValidationError.MissingBalance,
                DebtValidationError.NonPositiveInstallment,
                DebtValidationError.PaymentDayOutOfRange,
            ),
            DebtValidator.validate(draft),
        )
    }

    @Test
    fun `a valid draft becomes a debt with the given id`() {
        val debt = VALID.toDebtOrNull(id = 7)

        assertEquals(7L, debt?.id)
        assertEquals("Tarjeta", debt?.name)
        assertEquals(15, debt?.paymentDay)
    }

    @Test
    fun `an incomplete draft becomes no debt`() {
        assertEquals(null, VALID.copy(paymentDay = 40).toDebtOrNull(id = 0))
    }
}
