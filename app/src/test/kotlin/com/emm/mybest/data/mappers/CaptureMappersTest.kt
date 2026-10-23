package com.emm.mybest.data.mappers

import com.emm.mybest.domain.models.CapturedNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class CaptureMappersTest {

    @Test
    fun `a captured notification round-trips through the entity`() {
        val notification = CapturedNotification(
            id = 4,
            packageName = "com.bcp.innovacxion.yapeapp",
            title = " Yape ",
            text = "Te yapearon S/ 25.00\n",
            postedAt = Instant.fromEpochMilliseconds(1_792_710_900_123),
        )

        assertEquals(notification, notification.toEntity().toDomain())
    }

    @Test
    fun `a captured notification without title or text round-trips through the entity`() {
        val notification = CapturedNotification(
            id = 5,
            packageName = "com.interbank.plin",
            title = null,
            text = null,
            postedAt = Instant.fromEpochMilliseconds(0),
        )

        assertEquals(notification, notification.toEntity().toDomain())
    }
}
