package com.emm.mybest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emm.mybest.data.entities.Converters
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.HabitDao
import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.data.entities.HabitRecordDao
import com.emm.mybest.data.entities.HabitRecordEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity

@Database(
    entities = [
        DailyHabitEntity::class,
        DailyWeightEntity::class,
        ProgressPhotoEntity::class,
        HabitEntity::class,
        HabitRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyHabitDao(): DailyHabitDao
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun habitDao(): HabitDao
    abstract fun habitRecordDao(): HabitRecordDao
}
