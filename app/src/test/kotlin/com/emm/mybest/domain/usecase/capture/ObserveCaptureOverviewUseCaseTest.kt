package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.CaptureAppOption
import com.emm.mybest.domain.models.CaptureSummary
import com.emm.mybest.domain.models.InstalledApp
import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.domain.repository.InstalledAppsRepository
import com.emm.mybest.testing.FakeInstalledAppsRepository
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val YAPE = "com.bcp.innovacxion.yapeapp"
private const val PLIN = "com.interbank.plin"
private const val REMOVED = "com.removed.app"
private val YAPE_APP = InstalledApp(YAPE, "Yape")
private val PLIN_APP = InstalledApp(PLIN, "Plin")
private val POSTED_AT = Instant.parse("2026-10-23T23:15:00Z")

class ObserveCaptureOverviewUseCaseTest {

    private fun useCase(repository: FakeNotificationCaptureRepository) =
        ObserveCaptureOverviewUseCase(FakeInstalledAppsRepository(listOf(PLIN_APP, YAPE_APP)), repository)

    @Test
    fun `lists every installed app with its selection`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        val overview = useCase(repository)().first()

        assertEquals(
            listOf(CaptureAppOption(PLIN_APP, isSelected = false), CaptureAppOption(YAPE_APP, isSelected = true)),
            overview.apps,
        )
    }

    @Test
    fun `reflects a selection change`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        val useCase = useCase(repository)

        repository.setAppSelected(PLIN, isSelected = true)

        assertEquals(listOf(PLIN), useCase().first().apps.filter { it.isSelected }.map { it.app.packageName })
    }

    @Test
    fun `labels each captured notification with its app`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))
        repository.saveCaptured(PostedNotification(YAPE, "key", "Yape", "Te yapearon S/ 25.00", POSTED_AT))

        val entries = useCase(repository)().first().entries

        assertEquals(
            listOf("Yape" to "Te yapearon S/ 25.00"),
            entries.map { it.notification.title to it.notification.text },
        )
        assertEquals(listOf("Yape"), entries.map { it.appLabel })
    }

    @Test
    fun `falls back to the package name when the app is no longer installed`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(PostedNotification(REMOVED, "key", "Hola", null, POSTED_AT))

        val entries = useCase(repository)().first().entries

        assertEquals(listOf(REMOVED), entries.map { it.appLabel })
    }

    @Test
    fun `keeps the newest captured notification first`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(PostedNotification(YAPE, "key", "first", null, POSTED_AT))
        repository.saveCaptured(PostedNotification(PLIN, "key", "second", null, POSTED_AT + 1.minutes))

        val entries = useCase(repository)().first().entries

        assertEquals(listOf("second", "first"), entries.map { it.notification.title })
    }

    @Test
    fun `counts every selected app in the summary, installed or not`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE, REMOVED))

        val summary = useCase(repository)().first().summary

        assertEquals(CaptureSummary(2), summary)
    }

    @Test
    fun `shows no apps and labels notifications by package when the installed apps cannot be listed`() = runTest {
        val repository = FakeNotificationCaptureRepository()
        repository.saveCaptured(PostedNotification(YAPE, "key", "Yape", null, POSTED_AT))
        val failingApps = object : InstalledAppsRepository {
            override suspend fun list(): List<InstalledApp> = error("boom")
        }

        val overview = ObserveCaptureOverviewUseCase(failingApps, repository)().first()

        assertEquals(emptyList<CaptureAppOption>(), overview.apps)
        assertEquals(listOf(YAPE), overview.entries.map { it.appLabel })
    }

    @Test
    fun `does not swallow a cancellation while listing the installed apps`() = runTest {
        val cancellingApps = object : InstalledAppsRepository {
            override suspend fun list(): List<InstalledApp> = throw CancellationException("cancelled")
        }

        val result = runCatching {
            ObserveCaptureOverviewUseCase(
                cancellingApps,
                FakeNotificationCaptureRepository(),
            )().first()
        }

        assertTrue(result.exceptionOrNull() is CancellationException)
    }
}
