package com.emm.mybest.features.capture.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.InstalledApp
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import com.emm.mybest.domain.usecase.capture.ObserveCaptureOverviewUseCase
import com.emm.mybest.testing.FakeInstalledAppsRepository
import com.emm.mybest.testing.FakeNotificationAccessChecker
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

private val LIMA = TimeZone.of("America/Lima")
private const val YAPE = "com.bcp.innovacxion.yapeapp"
private const val PLIN = "com.interbank.plin"
private val YAPE_APP = InstalledApp(YAPE, "Yape")
private val PLIN_APP = InstalledApp(PLIN, "Plin")
private val FRIDAY_18_15_LIMA = Instant.parse("2026-10-23T23:15:00Z")

class NotificationCaptureViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repository: NotificationCaptureRepository = FakeNotificationCaptureRepository(),
        checker: FakeNotificationAccessChecker = FakeNotificationAccessChecker(),
    ) = NotificationCaptureViewModel(
        useCases = NotificationCaptureUseCases(
            observeOverview = ObserveCaptureOverviewUseCase(
                FakeInstalledAppsRepository(listOf(PLIN_APP, YAPE_APP)),
                repository,
            ),
            repository = repository,
            accessChecker = checker,
        ),
        appDayClock = AppDayClock(FixedClock(FRIDAY_18_15_LIMA), LIMA),
    )

    private fun failingRepository(delegate: FakeNotificationCaptureRepository): NotificationCaptureRepository =
        object : NotificationCaptureRepository by delegate {
            override suspend fun setAppSelected(packageName: String, isSelected: Boolean) = error("boom")

            override suspend fun clearSelectedApps() = error("boom")

            override suspend fun deleteAllCaptured() = error("boom")
        }

    private fun TestScope.send(
        viewModel: NotificationCaptureViewModel,
        vararg intents: NotificationCaptureIntent,
    ): NotificationCaptureState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        intents.forEach { viewModel.onIntent(it) }
        advanceUntilIdle()
        return viewModel.state.value
    }

    @Test
    fun `lists the installed apps unselected while capture is off`() = runTest {
        val state = send(viewModel())

        assertTrue(state.isLoaded)
        assertFalse(state.isCaptureActive)
        assertEquals(listOf("Plin" to false, "Yape" to false), state.apps.map { it.label to it.isSelected })
    }

    @Test
    fun `toggling an app on selects it and turns capture on`() = runTest {
        val repository = FakeNotificationCaptureRepository()

        val state = send(viewModel(repository), NotificationCaptureIntent.OnAppToggle(YAPE, isSelected = true))

        assertEquals(setOf(YAPE), repository.observeSelectedApps().first())
        assertEquals(listOf(YAPE), state.apps.filter { it.isSelected }.map { it.packageName })
        assertTrue(state.isCaptureActive)
    }

    @Test
    fun `capture stays active while only an app that is no longer installed is selected`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf("com.removed.app"))

        val state = send(viewModel(repository))

        assertTrue(state.isCaptureActive)
    }

    @Test
    fun `toggling an app off deselects it`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE, PLIN))

        val state = send(viewModel(repository), NotificationCaptureIntent.OnAppToggle(YAPE, isSelected = false))

        assertEquals(setOf(PLIN), repository.observeSelectedApps().first())
        assertEquals(listOf(PLIN), state.apps.filter { it.isSelected }.map { it.packageName })
    }

    @Test
    fun `turning capture off deselects every app but keeps what was captured`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE, PLIN))
        repository.saveCaptured(YAPE, "Yape", "Te yapearon S/ 25.00", FRIDAY_18_15_LIMA)

        val state = send(viewModel(repository), NotificationCaptureIntent.OnTurnOffClick)

        assertEquals(emptySet<String>(), repository.observeSelectedApps().first())
        assertFalse(state.isCaptureActive)
        assertEquals(1, state.notifications.size)
    }

    @Test
    fun `delete all asks for confirmation before deleting anything`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(YAPE, "Yape", "Te yapearon S/ 25.00", FRIDAY_18_15_LIMA)

        val state = send(viewModel(repository), NotificationCaptureIntent.OnDeleteAllClick)

        assertTrue(state.isDeleteConfirmationVisible)
        assertEquals(1, repository.observeCaptured().first().size)
    }

    @Test
    fun `dismissing the confirmation keeps every captured notification`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(YAPE, "Yape", null, FRIDAY_18_15_LIMA)

        val state = send(
            viewModel(repository),
            NotificationCaptureIntent.OnDeleteAllClick,
            NotificationCaptureIntent.OnDeleteAllDismiss,
        )

        assertFalse(state.isDeleteConfirmationVisible)
        assertEquals(1, state.notifications.size)
    }

    @Test
    fun `confirming delete all removes every captured notification`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))
        repository.saveCaptured(YAPE, "Yape", "uno", FRIDAY_18_15_LIMA)
        repository.saveCaptured(YAPE, "Yape", "dos", FRIDAY_18_15_LIMA)

        val state = send(
            viewModel(repository),
            NotificationCaptureIntent.OnDeleteAllClick,
            NotificationCaptureIntent.OnDeleteAllConfirm,
        )

        assertTrue(state.notifications.isEmpty())
        assertTrue(repository.observeCaptured().first().isEmpty())
        assertFalse(state.isDeleteConfirmationVisible)
        assertEquals(setOf(YAPE), repository.observeSelectedApps().first())
    }

    @Test
    fun `shows each notification as stored with its app, date and time`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(YAPE, " Yape ", "Te yapearon S/ 25.00 \n", FRIDAY_18_15_LIMA)

        val item = send(viewModel(repository)).notifications.single()

        assertEquals("YAPE", item.appLabel)
        assertEquals(" Yape ", item.title)
        assertEquals("Te yapearon S/ 25.00 \n", item.text)
        assertEquals("VIERNES 23 OCTUBRE", item.postedAtDateLabel)
        assertEquals("18:15", item.postedAtTimeLabel)
    }

    @Test
    fun `an absent title or text stays absent`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(YAPE, null, "solo texto", FRIDAY_18_15_LIMA)
        repository.saveCaptured(PLIN, "solo título", null, FRIDAY_18_15_LIMA)

        val items = send(viewModel(repository)).notifications

        assertEquals(listOf("solo título" to null, null to "solo texto"), items.map { it.title to it.text })
    }

    @Test
    fun `shows no notifications when nothing was captured`() = runTest {
        val state = send(viewModel())

        assertTrue(state.notifications.isEmpty())
    }

    @Test
    fun `reflects the notification access state`() = runTest {
        val checker = FakeNotificationAccessChecker(isAccessGranted = false)
        val viewModel = viewModel(checker = checker)
        assertFalse(send(viewModel).isAccessGranted)

        checker.isAccessGranted = true

        assertTrue(send(viewModel, NotificationCaptureIntent.OnResume).isAccessGranted)
    }

    @Test
    fun `asking for access opens the system notification access settings`() = runTest {
        val viewModel = viewModel()

        viewModel.effect.test {
            send(viewModel, NotificationCaptureIntent.OnOpenAccessSettingsClick)
            assertEquals(NotificationCaptureEffect.OpenAccessSettings, awaitItem())
        }
    }

    @Test
    fun `a failed selection reports the failure`() = runTest {
        val viewModel = viewModel(failingRepository(FakeNotificationCaptureRepository()))

        viewModel.effect.test {
            send(viewModel, NotificationCaptureIntent.OnAppToggle(YAPE, isSelected = true))
            assertEquals(NotificationCaptureEffect.ActionFailed, awaitItem())
        }
    }

    @Test
    fun `a failed delete all reports the failure and hides the confirmation`() = runTest {
        val viewModel = viewModel(failingRepository(FakeNotificationCaptureRepository()))

        viewModel.effect.test {
            val state = send(
                viewModel,
                NotificationCaptureIntent.OnDeleteAllClick,
                NotificationCaptureIntent.OnDeleteAllConfirm,
            )
            assertEquals(NotificationCaptureEffect.ActionFailed, awaitItem())
            assertFalse(state.isDeleteConfirmationVisible)
        }
    }

    @Test
    fun `a failed turn off reports the failure`() = runTest {
        val viewModel = viewModel(failingRepository(FakeNotificationCaptureRepository(setOf(YAPE))))

        viewModel.effect.test {
            send(viewModel, NotificationCaptureIntent.OnTurnOffClick)
            assertEquals(NotificationCaptureEffect.ActionFailed, awaitItem())
        }
    }
}
