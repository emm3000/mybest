package com.emm.mybest.data.entities

import androidx.room.TypeConverter
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.LocalDate

private const val YEAR_DIGITS = 4
private const val MONTH_DIGITS = 2
private const val MONTH_SEPARATOR = '-'

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

    @TypeConverter
    fun fromYearMonth(month: YearMonthValue): String =
        "${month.year.toString().padStart(YEAR_DIGITS, '0')}$MONTH_SEPARATOR" +
            month.month.toString().padStart(MONTH_DIGITS, '0')

    @TypeConverter
    fun toYearMonth(value: String): YearMonthValue {
        val (year, month) = value.split(MONTH_SEPARATOR)
        return YearMonthValue(year.toInt(), month.toInt())
    }
}
