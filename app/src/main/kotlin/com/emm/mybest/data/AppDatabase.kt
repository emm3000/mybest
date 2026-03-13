package com.emm.mybest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        HabitRecordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyHabitDao(): DailyHabitDao
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun habitDao(): HabitDao
    abstract fun habitRecordDao(): HabitRecordDao

    companion object {
        const val DB_NAME = "my_best_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_weight ADD COLUMN habit_id TEXT")
                db.execSQL("ALTER TABLE progress_photo ADD COLUMN habit_id TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_weight_habit_id ON daily_weight(habit_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progress_photo_habit_id ON progress_photo(habit_id)")
            }
        }
    }
}
