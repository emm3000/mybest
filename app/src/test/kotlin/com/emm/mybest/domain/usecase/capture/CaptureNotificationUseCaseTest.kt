package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val YAPE = "com.bcp.innovacxion.yapeapp"
private const val PLIN = "com.interbank.plin"
private const val KEY = "0|com.bcp.innovacxion.yapeapp|1|tag|10100"
private val POSTED_AT = Instant.parse("2026-10-23T23:15:00Z")

class CaptureNotificationUseCaseTest {

    @Test
    fun `a notification from a selected app is saved with title and text unchanged`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(title = " Yape ", text = "Te yapearon S/ 25.00 \n"))

        assertEquals(
            listOf(CapturedNotification(1, YAPE, " Yape ", "Te yapearon S/ 25.00 \n", POSTED_AT, KEY)),
            repository.observeCaptured().first(),
        )
    }

    @Test
    fun `the saved notification carries the posted key`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(key = "0|$YAPE|7|qa|10100"))

        assertEquals(listOf("0|$YAPE|7|qa|10100"), repository.observeCaptured().first().map { it.key })
    }

    @Test
    fun `two posts with the same key, title and text store one row`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted())
        CaptureNotificationUseCase(repository)(posted())

        assertEquals(1, repository.observeCaptured().first().size)
    }

    @Test
    fun `the same key with a different text stores two rows`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(text = "Te yapearon S/ 25.00"))
        CaptureNotificationUseCase(repository)(posted(text = "Te yapearon S/ 45.00"))

        assertEquals(2, repository.observeCaptured().first().size)
    }

    @Test
    fun `the same key with a different title stores two rows`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(title = "Yape"))
        CaptureNotificationUseCase(repository)(posted(title = "Yape Perú"))

        assertEquals(2, repository.observeCaptured().first().size)
    }

    @Test
    fun `only the latest row of a key is compared so text A, B, A stores three rows`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(text = "A", postedAt = POSTED_AT))
        CaptureNotificationUseCase(repository)(posted(text = "B", postedAt = POSTED_AT + 1.seconds))
        CaptureNotificationUseCase(repository)(posted(text = "A", postedAt = POSTED_AT + 2.seconds))

        assertEquals(listOf("A", "B", "A"), repository.observeCaptured().first().map { it.text }.reversed())
    }

    @Test
    fun `different keys with the same title and text store two rows`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(key = "k1"))
        CaptureNotificationUseCase(repository)(posted(key = "k2"))

        assertEquals(listOf("k2", "k1"), repository.observeCaptured().first().map { it.key })
    }

    @Test
    fun `a notification with a title and no text is saved`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(title = "Yape", text = null))

        assertEquals(listOf("Yape" to null), repository.observeCaptured().first().map { it.title to it.text })
    }

    @Test
    fun `a notification with a text and no title is saved`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(title = null, text = "Te yapearon S/ 25.00"))

        assertEquals(
            listOf(null to "Te yapearon S/ 25.00"),
            repository.observeCaptured().first().map { it.title to it.text },
        )
    }

    @Test
    fun `a notification with a blank title and a blank text is not saved`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(title = " ", text = ""))
        CaptureNotificationUseCase(repository)(posted(title = null, text = null))

        assertTrue(repository.observeCaptured().first().isEmpty())
    }

    @Test
    fun `a group summary from a selected app is not saved`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(isGroupSummary = true))

        assertTrue(repository.observeCaptured().first().isEmpty())
    }

    @Test
    fun `a notification from an unselected app is not saved`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf(YAPE))

        CaptureNotificationUseCase(repository)(posted(packageName = PLIN))

        assertTrue(repository.observeCaptured().first().isEmpty())
    }

    @Test
    fun `nothing is saved while no app is selected`() = runTest {
        val repository = FakeNotificationCaptureRepository()

        CaptureNotificationUseCase(repository)(posted())

        assertTrue(repository.observeCaptured().first().isEmpty())
    }
}

private fun posted(
    packageName: String = YAPE,
    key: String = KEY,
    title: String? = "Yape",
    text: String? = "Te yapearon S/ 25.00",
    isGroupSummary: Boolean = false,
    postedAt: Instant = POSTED_AT,
) = PostedNotification(packageName, key, title, text, postedAt, isGroupSummary)
