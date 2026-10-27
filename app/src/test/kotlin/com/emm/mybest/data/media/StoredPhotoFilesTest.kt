package com.emm.mybest.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StoredPhotoFilesTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val filesDir: File by lazy { folder.newFolder("files") }

    @Test
    fun `provider path of a stored photo resolves inside the photos directory`() {
        val file = storedPhotoFile(filesDir, "/internal_photos/IMG_1.jpg")

        assertEquals(File(filesDir, "photos/IMG_1.jpg"), file)
    }

    @Test
    fun `provider path under another root resolves to nothing`() {
        assertNull(storedPhotoFile(filesDir, "/other_root/IMG_1.jpg"))
    }

    @Test
    fun `provider path that leaves the photos directory resolves to nothing`() {
        assertNull(storedPhotoFile(filesDir, "/internal_photos/../databases/my_best_db"))
    }
}
