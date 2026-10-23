package com.emm.mybest.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emm.mybest.data.entities.Converters
import com.emm.mybest.data.entities.DailyCheckDao
import com.emm.mybest.data.entities.DailyCheckEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.data.entities.UserSetupDao
import com.emm.mybest.data.entities.UserSetupEntity

@Database(
    entities = [
        DailyWeightEntity::class,
        ProgressPhotoEntity::class,
        UserSetupEntity::class,
        DailyCheckEntity::class,
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun userSetupDao(): UserSetupDao
    abstract fun dailyCheckDao(): DailyCheckDao

    companion object {
        const val DB_NAME = "my_best_db"
    }
}
