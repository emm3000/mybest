package com.emm.mybest.data.capture

import com.emm.mybest.domain.models.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherAppsTest {

    @Test
    fun `launcher apps are sorted by label ignoring case`() {
        val apps = listOf(
            InstalledApp("com.interbank.plin", "Plin"),
            InstalledApp("com.bcp.bancadigital", "banca Móvil BCP"),
            InstalledApp("com.bcp.innovacxion.yapeapp", "Yape"),
        )

        assertEquals(listOf("banca Móvil BCP", "Plin", "Yape"), apps.sortedByLabel().map { it.label })
    }

    @Test
    fun `an app with two launcher activities is listed once`() {
        val apps = listOf(
            InstalledApp("com.bcp.innovacxion.yapeapp", "Yape"),
            InstalledApp("com.bcp.innovacxion.yapeapp", "Yape"),
        )

        assertEquals(1, apps.sortedByLabel().size)
    }
}
