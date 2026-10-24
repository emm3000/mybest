package com.emm.mybest.data

import com.emm.mybest.data.entities.CaptureAppEntity
import com.emm.mybest.data.entities.CapturedNotificationEntity
import com.emm.mybest.data.entities.NotificationCaptureDao
import com.emm.mybest.domain.models.PostedNotification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

private const val YAPE = "com.bcp.innovacxion.yapeapp"

class NotificationCaptureRepositoryImplTest {

    private val dao = mockk<NotificationCaptureDao>(relaxUnitFun = true)
    private val repository = NotificationCaptureRepositoryImpl(dao)

    @Test
    fun `selecting an app stores it`() = runTest {
        repository.setAppSelected(YAPE, isSelected = true)

        coVerify { dao.insertApp(CaptureAppEntity(YAPE)) }
    }

    @Test
    fun `deselecting an app removes it`() = runTest {
        repository.setAppSelected(YAPE, isSelected = false)

        coVerify { dao.deleteApp(YAPE) }
    }

    @Test
    fun `observeSelectedApps emits the stored package names as a set`() = runTest {
        every { dao.observeAppPackageNames() } returns flowOf(listOf(YAPE, "com.interbank.plin"))

        assertEquals(setOf(YAPE, "com.interbank.plin"), repository.observeSelectedApps().first())
    }

    @Test
    fun `saveCaptured stores title and text exactly as given`() = runTest {
        coEvery { dao.insertCaptured(any()) } returns 1

        repository.saveCaptured(
            PostedNotification(YAPE, " Yape ", "Te yapearon S/ 25.00\n", Instant.fromEpochMilliseconds(1_000)),
        )

        coVerify {
            dao.insertCaptured(
                CapturedNotificationEntity(
                    packageName = YAPE,
                    title = " Yape ",
                    text = "Te yapearon S/ 25.00\n",
                    postedAtEpochMs = 1_000,
                ),
            )
        }
    }
}
