package com.emm.mybest.di

import androidx.room.Room
import androidx.work.WorkManager
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.core.coroutines.DefaultCoroutineDispatchers
import com.emm.mybest.data.AppDatabase
import com.emm.mybest.data.BackupRepositoryImpl
import com.emm.mybest.data.ComplianceRepositoryImpl
import com.emm.mybest.data.ExercisePlanRepositoryImpl
import com.emm.mybest.data.MIGRATION_2_3
import com.emm.mybest.data.MIGRATION_3_4
import com.emm.mybest.data.MealPlanRepositoryImpl
import com.emm.mybest.data.PhotoRepositoryImpl
import com.emm.mybest.data.UserPreferencesRepositoryImpl
import com.emm.mybest.data.WeightRepositoryImpl
import com.emm.mybest.data.entities.ExerciseComplianceDao
import com.emm.mybest.data.entities.ExercisePlanDao
import com.emm.mybest.data.entities.MealComplianceDao
import com.emm.mybest.data.entities.MealPlanDao
import com.emm.mybest.data.reminder.WeightReminderSchedulerImpl
import com.emm.mybest.domain.media.MediaManager
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.BackupRepository
import com.emm.mybest.domain.repository.ComplianceRepository
import com.emm.mybest.domain.repository.ExercisePlanRepository
import com.emm.mybest.domain.repository.MealPlanRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateDefaultReminderTimeUseCase
import com.emm.mybest.domain.usecase.compliance.GetCompletionStreakUseCase
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.exercise.UpsertExerciseRoutineUseCase
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.GetPhotosOverviewUseCase
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import com.emm.mybest.domain.usecase.preferences.ObserveDailySlotTimesUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.domain.usecase.weight.GetNearestWeightOnDateUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.features.diet.presentation.MealPlanViewModel
import com.emm.mybest.features.exercise.presentation.ExercisePlanViewModel
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.home.presentation.HomeUseCases
import com.emm.mybest.features.home.presentation.HomeViewModel
import com.emm.mybest.features.insights.presentation.InsightsViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.photo.presentation.PhotosViewModel
import com.emm.mybest.features.photo.presentation.viewer.PhotoViewerViewModel
import com.emm.mybest.features.settings.presentation.SettingsViewModel
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

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
    single<MealPlanDao> { get<AppDatabase>().mealPlanDao() }
    single<ExercisePlanDao> { get<AppDatabase>().exercisePlanDao() }
    single<MealComplianceDao> { get<AppDatabase>().mealComplianceDao() }
    single<ExerciseComplianceDao> { get<AppDatabase>().exerciseComplianceDao() }

    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }

    single<WeightRepository> { WeightRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(androidContext(), get(), get()) }
    single<BackupRepository> { BackupRepositoryImpl(androidContext(), get(), get()) }

    single<MealPlanRepository> { MealPlanRepositoryImpl(get()) }
    single<ExercisePlanRepository> { ExercisePlanRepositoryImpl(get()) }
    single<ComplianceRepository> { ComplianceRepositoryImpl(get(), get()) }
    single { WorkManager.getInstance(androidContext()) }
    single<WeightReminderScheduler> { WeightReminderSchedulerImpl(get()) }

    factory { GetInsightsUseCase(get(), get()) }
    factory { ExportDatabaseBackupUseCase(get()) }
    factory { RestoreDatabaseBackupUseCase(get()) }
    factory { UpdateDefaultReminderTimeUseCase(get(), get()) }

    // Diet use cases
    factory { GetWeeklyMealPlanUseCase(get()) }
    factory { UpsertMealUseCase(get()) }

    // Exercise use cases
    factory { GetWeeklyExercisePlanUseCase(get()) }
    factory { UpsertExerciseRoutineUseCase(get()) }

    // Compliance use cases
    factory { ObserveDailyComplianceUseCase(get()) }
    factory { ToggleMealComplianceUseCase(get()) }
    factory { ToggleExerciseComplianceUseCase(get()) }
    factory { GetCompletionStreakUseCase(get()) }
    factory { ObserveDailySlotTimesUseCase(get()) }
    factory { HomeUseCases(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    // Weight use cases
    factory { SaveWeightUseCase(get()) }
    factory { DeleteWeightByDateUseCase(get()) }
    factory { ObserveWeightProgressUseCase(get()) }
    factory { GetNearestWeightOnDateUseCase(get()) }

    // Photo use cases
    factory { SavePhotosUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { ObservePhotosUseCase(get()) }
    factory { GetPhotosOverviewUseCase(get(), get()) }

    // History use cases
    factory { GetHistoryUseCase(get(), get()) }

    single { MediaManager(androidContext()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }

    viewModel { HomeViewModel(get()) }
    viewModel { AddWeightViewModel(get(), get()) }
    viewModel { PhotosViewModel(get(), get(), get(), Clock.System) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { InsightsViewModel(get()) }
    viewModel { (beforeId: String, afterId: String) ->
        ComparePhotosViewModel(beforeId, afterId, get())
    }
    viewModel { (initialPhotoId: String) -> PhotoViewerViewModel(initialPhotoId, get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { MealPlanViewModel(get(), get()) }
    viewModel { ExercisePlanViewModel(get(), get()) }
}
