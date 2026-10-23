package com.emm.mybest.data.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationBodyTest {

    @Test
    fun `the big text wins over the text when present`() {
        assertEquals(
            "Te yapearon S/ 25.00 de Ana",
            bodyOf(bigText = "Te yapearon S/ 25.00 de Ana", text = "Te yapearon"),
        )
    }

    @Test
    fun `the text is used when there is no big text`() {
        assertEquals(" Te yapearon \n", bodyOf(bigText = null, text = " Te yapearon \n"))
    }

    @Test
    fun `the body is null when there is neither`() {
        assertNull(bodyOf(bigText = null, text = null))
    }
}
