package com.emm.mybest.data.capture

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class PostedNotificationMappingTest {

    @Test
    fun `the package and the post time are copied`() {
        val posted = sbn(packageName = "com.interbank.plin", postTime = 1_700_000_000_123L).toPostedNotification()

        assertEquals("com.interbank.plin", posted.packageName)
        assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_123L), posted.postedAt)
    }

    @Test
    fun `the title is copied from the title extra`() {
        val posted = sbn(extras = extras(title = "Plin")).toPostedNotification()

        assertEquals("Plin", posted.title)
    }

    @Test
    fun `the big text wins over the text`() {
        val posted = sbn(
            extras = extras(bigText = "Recibiste S/ 30.00 de Ana", text = "Recibiste"),
        ).toPostedNotification()

        assertEquals("Recibiste S/ 30.00 de Ana", posted.text)
    }

    @Test
    fun `the text is used when there is no big text`() {
        val posted = sbn(extras = extras(text = "Recibiste S/ 30.00")).toPostedNotification()

        assertEquals("Recibiste S/ 30.00", posted.text)
    }

    @Test
    fun `the title and the text are null when the extras have neither`() {
        val posted = sbn(extras = extras()).toPostedNotification()

        assertNull(posted.title)
        assertNull(posted.text)
    }

    private fun extras(title: String? = null, bigText: String? = null, text: String? = null): Bundle {
        val bundle = mockk<Bundle>()
        every { bundle.getCharSequence(Notification.EXTRA_TITLE) } returns title
        every { bundle.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns bigText
        every { bundle.getCharSequence(Notification.EXTRA_TEXT) } returns text
        return bundle
    }

    private fun sbn(
        packageName: String = "com.android.shell",
        postTime: Long = 0L,
        extras: Bundle = extras(),
    ): StatusBarNotification {
        val notification = mockk<Notification>()
        notification.extras = extras
        val sbn = mockk<StatusBarNotification>()
        every { sbn.notification } returns notification
        every { sbn.packageName } returns packageName
        every { sbn.postTime } returns postTime
        return sbn
    }
}
