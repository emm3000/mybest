package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.InstallmentReminder
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeUserPreferencesRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val MORNING = LocalTime(9, 0)
private val CARD = Debt(id = 1, name = "Tarjeta", balance = Money(90_000), installment = Money(15_000), paymentDay = 15)

class GetInstallmentRemindersUseCaseTest {

    private fun useCase(
        appDay: LocalDate,
        debts: FakeDebtRepository,
        preferences: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
    ) = GetInstallmentRemindersUseCase(
        debts,
        preferences,
        AppDayClock(FixedClock(LocalDateTime(appDay, MORNING).toInstant(LIMA)), LIMA),
    )

    @Test
    fun `reminds an installment due on the 15th on app day 12`() = runTest {
        val reminders = useCase(LocalDate(2026, 10, 12), FakeDebtRepository(listOf(CARD)))()

        assertEquals(listOf(InstallmentReminder(CARD, LocalDate(2026, 10, 15))), reminders)
    }

    @Test
    fun `does not remind an installment due on the 15th on app day 13`() = runTest {
        assertTrue(useCase(LocalDate(2026, 10, 13), FakeDebtRepository(listOf(CARD)))().isEmpty())
    }

    @Test
    fun `does not remind an installment already paid this month`() = runTest {
        val paid = DebtPayment(CARD.id, YearMonthValue(2026, 10), CARD.installment)

        assertTrue(useCase(LocalDate(2026, 10, 12), FakeDebtRepository(listOf(CARD), listOf(paid)))().isEmpty())
    }

    @Test
    fun `reminds a payment day 31 on the 27th of a 30-day month for the 30th`() = runTest {
        val debt = CARD.copy(paymentDay = 31)

        val reminders = useCase(LocalDate(2026, 9, 27), FakeDebtRepository(listOf(debt)))()

        assertEquals(listOf(InstallmentReminder(debt, LocalDate(2026, 9, 30))), reminders)
    }

    @Test
    fun `reminds next month's installment although this month's is paid`() = runTest {
        val debt = CARD.copy(paymentDay = 1)
        val paid = DebtPayment(CARD.id, YearMonthValue(2026, 9), CARD.installment)

        val reminders = useCase(LocalDate(2026, 9, 28), FakeDebtRepository(listOf(debt), listOf(paid)))()

        assertEquals(listOf(InstallmentReminder(debt, LocalDate(2026, 10, 1))), reminders)
    }

    @Test
    fun `does not remind a debt that is paid off`() = runTest {
        val paidOff = CARD.copy(balance = Money.ZERO)

        assertTrue(useCase(LocalDate(2026, 10, 12), FakeDebtRepository(listOf(paidOff)))().isEmpty())
    }

    @Test
    fun `reminds nothing without debts`() = runTest {
        assertTrue(useCase(LocalDate(2026, 10, 12), FakeDebtRepository())().isEmpty())
    }

    @Test
    fun `reminds nothing when notifications are off`() = runTest {
        val preferences = FakeUserPreferencesRepository(isNotificationsEnabled = false)

        assertTrue(useCase(LocalDate(2026, 10, 12), FakeDebtRepository(listOf(CARD)), preferences)().isEmpty())
    }
}
