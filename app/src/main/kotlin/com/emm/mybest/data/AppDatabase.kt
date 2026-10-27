package com.emm.mybest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emm.mybest.data.entities.CaptureAppEntity
import com.emm.mybest.data.entities.CapturedNotificationEntity
import com.emm.mybest.data.entities.Converters
import com.emm.mybest.data.entities.DailyCheckDao
import com.emm.mybest.data.entities.DailyCheckEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.DebtDao
import com.emm.mybest.data.entities.DebtEntity
import com.emm.mybest.data.entities.DebtPaymentEntity
import com.emm.mybest.data.entities.GoalContributionEntity
import com.emm.mybest.data.entities.NoOutingAnswerDao
import com.emm.mybest.data.entities.NoOutingAnswerEntity
import com.emm.mybest.data.entities.NoteDao
import com.emm.mybest.data.entities.NoteEntity
import com.emm.mybest.data.entities.NotificationCaptureDao
import com.emm.mybest.data.entities.OutingDao
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.data.entities.PurchaseGoalDao
import com.emm.mybest.data.entities.PurchaseGoalEntity
import com.emm.mybest.data.entities.UserSetupDao
import com.emm.mybest.data.entities.UserSetupEntity
import com.emm.mybest.data.entities.WonNightDao
import com.emm.mybest.data.entities.WonNightEntity

@Database(
    entities = [
        DailyWeightEntity::class,
        ProgressPhotoEntity::class,
        UserSetupEntity::class,
        DailyCheckEntity::class,
        OutingEntity::class,
        WonNightEntity::class,
        NoOutingAnswerEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        PurchaseGoalEntity::class,
        GoalContributionEntity::class,
        CaptureAppEntity::class,
        CapturedNotificationEntity::class,
        NoteEntity::class,
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
@Suppress("TooManyFunctions")
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun userSetupDao(): UserSetupDao
    abstract fun dailyCheckDao(): DailyCheckDao
    abstract fun outingDao(): OutingDao
    abstract fun wonNightDao(): WonNightDao
    abstract fun noOutingAnswerDao(): NoOutingAnswerDao
    abstract fun debtDao(): DebtDao
    abstract fun purchaseGoalDao(): PurchaseGoalDao
    abstract fun notificationCaptureDao(): NotificationCaptureDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val DB_NAME = "my_best_db"
        const val SCHEMA_VERSION = 12
        const val V1_SCHEMA_VERSION = 10
    }
}
