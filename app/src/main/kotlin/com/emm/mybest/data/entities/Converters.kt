package com.emm.mybest.data.entities

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = value.let(LocalDate::parse)

    @TypeConverter
    fun fromPhotoType(type: PhotoType): String = type.name

    @TypeConverter
    fun toPhotoType(value: String): PhotoType = when (value) {
        "FACE" -> PhotoType.FACE
        else -> PhotoType.TRUNK
    }
}
