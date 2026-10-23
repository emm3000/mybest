package com.emm.mybest.data.entities

import androidx.room.TypeConverter
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = value.let(LocalDate::parse)

    @TypeConverter
    fun fromPhotoType(type: PhotoType): String = type.name

    @TypeConverter
    fun toPhotoType(value: String): PhotoType =
        PhotoType.entries.firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown PhotoType value: $value")

    @TypeConverter
    fun fromDailyCheckType(type: DailyCheckType): String = type.name

    @TypeConverter
    fun toDailyCheckType(value: String): DailyCheckType =
        DailyCheckType.entries.firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown DailyCheckType value: $value")

    @TypeConverter
    fun fromOutingKind(kind: OutingKind): String = kind.name

    @TypeConverter
    fun toOutingKind(value: String): OutingKind =
        OutingKind.entries.firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown OutingKind value: $value")
}
