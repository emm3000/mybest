package com.emm.mybest.data.reminder

import com.emm.mybest.core.money.Money
import com.emm.mybest.core.navigation.ACTION_DEBTS
import com.emm.mybest.core.notification.DEBT_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.InstallmentReminder
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private val DUE_DATE = LocalDate(2026, 10, 15)
private val CARD = Debt(id = 1, name = "Tarjeta", balance = Money(90_000), installment = Money(15_000), paymentDay = 15)
private val LOAN = CARD.copy(id = 2, name = "Préstamo")

class InstallmentReminderNotificationTest {

    @Test
    fun `opens the debts screen on the debt reminder channel`() {
        val notification = InstallmentReminder(CARD, DUE_DATE).toReminderNotification()

        assertEquals(DEBT_REMINDER_CHANNEL_ID, notification.channelId)
        assertEquals(ACTION_DEBTS, notification.content.action)
    }

    @Test
    fun `gives each debt its own notification`() {
        val card = InstallmentReminder(CARD, DUE_DATE).toReminderNotification()
        val loan = InstallmentReminder(LOAN, DUE_DATE).toReminderNotification()

        assertNotEquals(card.notificationId, loan.notificationId)
    }
}
