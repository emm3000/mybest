package com.emm.mybest.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emm.mybest.data.entities.Converters
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ExerciseComplianceDao
import com.emm.mybest.data.entities.ExerciseComplianceEntity
import com.emm.mybest.data.entities.ExercisePlanDao
import com.emm.mybest.data.entities.ExercisePlanEntryEntity
import com.emm.mybest.data.entities.MealComplianceDao
import com.emm.mybest.data.entities.MealComplianceEntity
import com.emm.mybest.data.entities.MealPlanDao
import com.emm.mybest.data.entities.MealPlanEntryEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity

@Database(
    entities = [
        DailyWeightEntity::class,
        ProgressPhotoEntity::class,
        MealPlanEntryEntity::class,
        ExercisePlanEntryEntity::class,
        MealComplianceEntity::class,
        ExerciseComplianceEntity::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun exercisePlanDao(): ExercisePlanDao
    abstract fun mealComplianceDao(): MealComplianceDao
    abstract fun exerciseComplianceDao(): ExerciseComplianceDao

    companion object {
        const val DB_NAME = "my_best_db"
    }
}
