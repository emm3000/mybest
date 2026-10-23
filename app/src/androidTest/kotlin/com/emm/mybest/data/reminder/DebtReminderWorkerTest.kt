package com.emm.mybest.data.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.reminder.GetInstallmentRemindersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Instant

private val LIMA = TimeZone.of("America/Lima")
private val THREE_DAYS_BEFORE_THE_15TH = LocalDateTime(2026, 10, 12, 9, 0).toInstant(LIMA)
private val CARD = Debt(id = 1, name = "Tarjeta", balance = Money(90_000), installment = Money(15_000), paymentDay = 15)
private val LOAN = CARD.copy(id = 2, name = "Préstamo", balance = Money(30_000), installment = Money(42_050))

class DebtReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        notificationManager().cancelAll()
        stopKoin()
        startKoin {
            modules(
                module {
                    single { ReminderNotifier(context) }
                    single {
                        GetInstallmentRemindersUseCase(
                            UnpaidDebtRepository(listOf(CARD, LOAN)),
                            NotificationsOnPreferences(),
                            AppDayClock(StoppedClock(THREE_DAYS_BEFORE_THE_15TH), LIMA),
                        )
                    }
                },
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun doWork_posts_one_notification_per_installment_due_in_three_days_with_the_amount_owed() = runBlocking {
        val worker = TestListenableWorkerBuilder.from(context, DebtReminderWorker::class.java).build()

        val result = worker.doWork()

        val active = notificationManager().awaitActiveNotifications { active -> active.size == 2 }
        val texts = active.map { it.notification.extras.getString(Notification.EXTRA_TEXT) }
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(
            setOf("Tu cuota de Tarjeta (S/ 150.00) vence el 15", "Tu cuota de Préstamo (S/ 300.00) vence el 15"),
            texts.toSet(),
        )
    }
}

private class UnpaidDebtRepository(private val debts: List<Debt>) : DebtRepository {

    override fun observeDebts(): Flow<List<Debt>> = flowOf(debts)

    override fun observePayments(): Flow<List<DebtPayment>> = flowOf(emptyList())

    override suspend fun saveDebt(debt: Debt): Long = debt.id

    override suspend fun deleteDebt(id: Long) = Unit

    override suspend fun markInstallmentPaid(debtId: Long, month: YearMonthValue) = Unit
}

private class NotificationsOnPreferences : UserPreferencesRepository {

    override val notificationsEnabled: Flow<Boolean> = flowOf(true)

    override suspend fun updateNotificationsEnabled(enabled: Boolean) = Unit
}

private class StoppedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
