package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMappersTest {

    @Test
    fun `weight entity maps to domain`() {
        val weightEntity = DailyWeightEntity(
            id = "w1",
            date = LocalDate(2026, 3, 8),
            weight = 70.5f,
            photoPath = "/tmp/w.jpg",
            note = "ok",
        )

        val weight = weightEntity.toDomain()

        assertEquals("w1", weight.id)
        assertEquals(70.5f, weight.weight)
        assertEquals("/tmp/w.jpg", weight.photoPath)
    }

    @Test
    fun `photo entity maps to domain`() {
        val photoEntity = ProgressPhotoEntity(
            id = "p1",
            date = LocalDate(2026, 3, 8),
            type = PhotoType.FACE,
            photoPath = "/tmp/p.jpg",
            createdAt = 456L,
        )

        val photo = photoEntity.toDomain()

        assertEquals("p1", photo.id)
        assertEquals(PhotoType.FACE, photo.type)
        assertEquals(456L, photo.createdAt)
    }

    @Test
    fun `new progress photo maps to entity`() {
        val input = NewProgressPhoto(
            photoPath = "/tmp/new.jpg",
            type = PhotoType.TRUNK,
            date = LocalDate(2026, 3, 8),
        )

        val entity = input.toEntity()

        assertEquals(PhotoType.TRUNK, entity.type)
        assertEquals("/tmp/new.jpg", entity.photoPath)
        assertEquals(LocalDate(2026, 3, 8), entity.date)
    }

    @Test
    fun `photo entity type is preserved through domain mapping`() {
        PhotoType.entries.forEach { type ->
            val entity = ProgressPhotoEntity(
                id = "x",
                date = LocalDate(2026, 1, 1),
                type = type,
                photoPath = "/tmp/x.jpg",
            )
            assertEquals(type, entity.toDomain().type)
        }
    }
}
