package com.emm.mybest.di

import android.util.Log
import androidx.room.Room
import androidx.work.WorkManager
import com.emm.mybest.core.coroutines.APP_SCOPE_NAME
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.core.coroutines.DefaultCoroutineDispatchers
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.data.AppDatabase
import com.emm.mybest.data.BackupRepositoryImpl
import com.emm.mybest.data.DailyCheckRepositoryImpl
import com.emm.mybest.data.DebtRepositoryImpl
import com.emm.mybest.data.MIGRATION_2_3
import com.emm.mybest.data.MIGRATION_3_4
import com.emm.mybest.data.NotificationCaptureRepositoryImpl
import com.emm.mybest.data.OutingRepositoryImpl
import com.emm.mybest.data.PhotoRepositoryImpl
import com.emm.mybest.data.PurchaseGoalRepositoryImpl
import com.emm.mybest.data.SetupRepositoryImpl
import com.emm.mybest.data.UserPreferencesRepositoryImpl
import com.emm.mybest.data.WeightRepositoryImpl
import com.emm.mybest.data.capture.InstalledAppsRepositoryImpl
import com.emm.mybest.data.capture.NotificationAccessCheckerImpl
import com.emm.mybest.data.reminder.CheckInReminderSchedulerImpl
import com.emm.mybest.data.reminder.PreOutingReminderSchedulerImpl
import com.emm.mybest.data.reminder.ReminderNotifier
import com.emm.mybest.data.reminder.WeightReminderSchedulerImpl
import com.emm.mybest.domain.media.MediaManager
import com.emm.mybest.domain.reminder.CheckInReminderScheduler
import com.emm.mybest.domain.reminder.PreOutingReminderScheduler
import com.emm.mybest.domain.reminder.ReminderDelays
import com.emm.mybest.domain.reminder.WeekendReminderSchedulers
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.BackupRepository
import com.emm.mybest.domain.repository.DailyCheckRepository
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.repository.InstalledAppsRepository
import com.emm.mybest.domain.repository.NotificationAccessChecker
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.usecase.UpdateWeighInReminderTimeUseCase
import com.emm.mybest.domain.usecase.capture.CaptureNotificationUseCase
import com.emm.mybest.domain.usecase.capture.ObserveCaptureOverviewUseCase
import com.emm.mybest.domain.usecase.capture.ObserveCaptureSummaryUseCase
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.checkin.RecordNoOutingUseCase
import com.emm.mybest.domain.usecase.checkin.RecordUnplannedOutingUseCase
import com.emm.mybest.domain.usecase.checkin.SaveCheckInUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveDailyChecksUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.ToggleDailyCheckUseCase
import com.emm.mybest.domain.usecase.debts.MarkInstallmentPaidUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtListUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import com.emm.mybest.domain.usecase.debts.SaveDebtUseCase
import com.emm.mybest.domain.usecase.goal.AddGoalContributionUseCase
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import com.emm.mybest.domain.usecase.goal.SavePurchaseGoalUseCase
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.HistoryBodySources
import com.emm.mybest.domain.usecase.history.HistoryNightSources
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.GetPhotosOverviewUseCase
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import com.emm.mybest.domain.usecase.preouting.GetEstimatedOutingCostUseCase
import com.emm.mybest.domain.usecase.preouting.HasDecisionOnUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNearestInstallmentUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNotSpentThisMonthUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveOutingMonthUseCase
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import com.emm.mybest.domain.usecase.preouting.ObservePurchaseGoalLeftUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import com.emm.mybest.domain.usecase.preouting.PlanOutingUseCase
import com.emm.mybest.domain.usecase.preouting.PreOutingDecisionUseCases
import com.emm.mybest.domain.usecase.preouting.PreOutingUseCases
import com.emm.mybest.domain.usecase.preouting.RecordWonNightUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase
import com.emm.mybest.domain.usecase.reminder.IsCheckInReminderDueUseCase
import com.emm.mybest.domain.usecase.reminder.IsPreOutingReminderDueUseCase
import com.emm.mybest.domain.usecase.reminder.SyncRemindersUseCase
import com.emm.mybest.domain.usecase.summary.ObserveWeekDebtUseCase
import com.emm.mybest.domain.usecase.summary.ObserveWeekMoneyUseCase
import com.emm.mybest.domain.usecase.summary.ObserveWeekOutingsUseCase
import com.emm.mybest.domain.usecase.summary.ObserveWeekRecoveryUseCase
import com.emm.mybest.domain.usecase.summary.ObserveWeeklySummaryUseCase
import com.emm.mybest.domain.usecase.summary.WeeklySummarySections
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.domain.usecase.weight.GetNearestWeightOnDateUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.features.capture.presentation.NotificationCaptureUseCases
import com.emm.mybest.features.capture.presentation.NotificationCaptureViewModel
import com.emm.mybest.features.checkin.presentation.CheckInUseCases
import com.emm.mybest.features.checkin.presentation.CheckInViewModel
import com.emm.mybest.features.debts.presentation.DebtsUseCases
import com.emm.mybest.features.debts.presentation.DebtsViewModel
import com.emm.mybest.features.goal.presentation.PurchaseGoalUseCases
import com.emm.mybest.features.goal.presentation.PurchaseGoalViewModel
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.home.presentation.HomeUseCases
import com.emm.mybest.features.home.presentation.HomeViewModel
import com.emm.mybest.features.insights.presentation.InsightsViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.photo.presentation.PhotosViewModel
import com.emm.mybest.features.photo.presentation.viewer.PhotoViewerViewModel
import com.emm.mybest.features.preouting.presentation.PreOutingViewModel
import com.emm.mybest.features.settings.presentation.SettingsGoalsEditor
import com.emm.mybest.features.settings.presentation.SettingsRemindersEditor
import com.emm.mybest.features.settings.presentation.SettingsRowSources
import com.emm.mybest.features.settings.presentation.SettingsViewModel
import com.emm.mybest.features.summary.presentation.SummaryViewModel
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Clock

private const val TAG = "AppModule"

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single { get<AppDatabase>().dailyWeightDao() }
    single { get<AppDatabase>().progressPhotoDao() }

    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }

    single { AppDayClock(Clock.System, TimeZone.currentSystemDefault()) }

    single<WeightRepository> { WeightRepositoryImpl(get(), get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(androidContext(), get(), get()) }
    single<BackupRepository> { BackupRepositoryImpl(androidContext(), get(), get()) }
    single { WorkManager.getInstance(androidContext()) }
    single<WeightReminderScheduler> { WeightReminderSchedulerImpl(get()) }

    single { get<AppDatabase>().userSetupDao() }
    single { get<AppDatabase>().dailyCheckDao() }
    single<SetupRepository> { SetupRepositoryImpl(get()) }
    single<DailyCheckRepository> { DailyCheckRepositoryImpl(get()) }

    single { get<AppDatabase>().outingDao() }
    single { get<AppDatabase>().wonNightDao() }
    single { get<AppDatabase>().noOutingAnswerDao() }
    single<OutingRepository> { OutingRepositoryImpl(get(), get(), get()) }

    single { get<AppDatabase>().debtDao() }
    single { get<AppDatabase>().purchaseGoalDao() }
    single<DebtRepository> { DebtRepositoryImpl(get()) }
    single<PurchaseGoalRepository> { PurchaseGoalRepositoryImpl(get()) }

    single { get<AppDatabase>().notificationCaptureDao() }
    single<NotificationCaptureRepository> { NotificationCaptureRepositoryImpl(get()) }
    single<InstalledAppsRepository> { InstalledAppsRepositoryImpl(androidContext().packageManager, get()) }
    single<NotificationAccessChecker> { NotificationAccessCheckerImpl(androidContext()) }
    factory { CaptureNotificationUseCase(get()) }
    factory { ObserveCaptureOverviewUseCase(get(), get()) }
    factory { ObserveCaptureSummaryUseCase(get()) }
    factory { NotificationCaptureUseCases(get(), get(), get()) }
    viewModel { NotificationCaptureViewModel(get(), get()) }

    factory { GetInsightsUseCase(get(), get()) }
    factory { ExportDatabaseBackupUseCase(get()) }
    factory { RestoreDatabaseBackupUseCase(get()) }
    factory { UpdateWeighInReminderTimeUseCase(get(), get()) }

    factory { ObserveDailyChecksUseCase(get()) }
    factory { ObserveWeekProgressUseCase(get(), get()) }
    factory { ToggleDailyCheckUseCase(get()) }
    factory { HomeUseCases(get(), get(), get(), get(), get()) }

    // Settings use cases
    factory { ObserveSetupUseCase(get()) }
    factory { UpdateSetupUseCase(get()) }
    factory { SettingsGoalsEditor(get(), get()) }
    factory { SettingsRemindersEditor(get(), get(), get()) }

    // Weight use cases
    factory { SaveWeightUseCase(get()) }
    factory { DeleteWeightByDateUseCase(get()) }
    factory { ObserveWeightProgressUseCase(get()) }
    factory { ObserveWeighInTrendUseCase(get(), get()) }
    factory { GetNearestWeightOnDateUseCase(get()) }

    // Photo use cases
    factory { SavePhotosUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { ObservePhotosUseCase(get()) }
    factory { GetPhotosOverviewUseCase(get(), get()) }

    // History use cases
    factory { GetHistoryUseCase(HistoryBodySources(get(), get()), HistoryNightSources(get(), get()), get()) }

    factory { GetEstimatedOutingCostUseCase(get(), get()) }
    factory { ObserveNotSpentThisMonthUseCase(get(), get()) }
    factory { ObserveOutingMonthUseCase(get(), get(), get()) }
    factory { ObserveNearestInstallmentUseCase(get(), get(), get()) }
    factory { ObservePurchaseGoalLeftUseCase(get()) }
    factory { PreOutingUseCases(get(), get(), get(), get(), get()) }
    factory { ObservePreOutingSnapshotUseCase(get()) }
    factory { HasDecisionOnUseCase(get()) }
    factory { PlanOutingUseCase(get(), get(), get()) }
    factory { RecordWonNightUseCase(get(), get(), get(), get()) }
    factory { ObserveTonightUseCase(get(), get()) }
    factory { PreOutingDecisionUseCases(get(), get(), get()) }
    viewModel { PreOutingViewModel(get(), get(), get()) }

    single { MediaManager(androidContext()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { AddWeightViewModel(get(), get(), get()) }
    viewModel { PhotosViewModel(get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get(), get()) }
    viewModel { InsightsViewModel(get()) }
    viewModel { (beforeId: String, afterId: String) ->
        ComparePhotosViewModel(beforeId, afterId, get())
    }
    viewModel { (initialPhotoId: String) -> PhotoViewerViewModel(initialPhotoId, get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }

    factory { ObserveDebtListUseCase(get(), get()) }
    factory { ObserveDebtSummaryUseCase(get()) }
    factory { SaveDebtUseCase(get()) }
    factory { MarkInstallmentPaidUseCase(get(), get()) }
    factory { DebtsUseCases(get(), get(), get(), get()) }
    viewModel { DebtsViewModel(get()) }

    factory { ObserveGoalProgressUseCase(get()) }
    factory { SavePurchaseGoalUseCase(get()) }
    factory { AddGoalContributionUseCase(get(), get()) }
    factory { SettingsRowSources(get(), get(), get()) }
    factory { PurchaseGoalUseCases(get(), get(), get(), get(), get()) }
    viewModel { PurchaseGoalViewModel(get()) }

    factory { ObserveRelapsesUseCase(get(), get()) }
    factory { ObserveReturnTimesUseCase(get(), get(), get()) }

    factory { ObserveCheckInTaskUseCase(get(), get()) }
    factory { SaveCheckInUseCase(get()) }
    factory { RecordUnplannedOutingUseCase(get()) }
    factory { RecordNoOutingUseCase(get()) }
    factory { CheckInUseCases(get(), get(), get(), get()) }
    viewModel { CheckInViewModel(get()) }

    factory { ObserveWeekOutingsUseCase(get()) }
    factory { ObserveWeekRecoveryUseCase(get(), get()) }
    factory { ObserveWeekDebtUseCase(get(), get()) }
    factory { ObserveWeekMoneyUseCase(get(), get(), get()) }
    factory { WeeklySummarySections(get(), get(), get(), get(), get()) }
    factory { ObserveWeeklySummaryUseCase(get()) }
    viewModel { SummaryViewModel(get(), get()) }

    single<CoroutineScope>(named(APP_SCOPE_NAME)) {
        val handler = CoroutineExceptionHandler { _, error -> Log.e(TAG, "Unhandled error in the app scope", error) }
        CoroutineScope(SupervisorJob() + get<CoroutineDispatchers>().default + handler)
    }
    single { ReminderDelays(Clock.System, TimeZone.currentSystemDefault()) }
    single { ReminderNotifier(androidContext()) }
    single<PreOutingReminderScheduler> { PreOutingReminderSchedulerImpl(get(), get()) }
    single<CheckInReminderScheduler> { CheckInReminderSchedulerImpl(get(), get()) }
    factory { WeekendReminderSchedulers(get(), get()) }
    factory { SyncRemindersUseCase(get(), get()) }
    factory { IsCheckInReminderDueUseCase(get(), get(), get()) }
    factory { IsPreOutingReminderDueUseCase(get()) }
}
