package com.emm.mybest.di

import androidx.room.Room
import com.emm.mybest.data.AppDatabase
import com.emm.mybest.data.DailyHabitRepositoryImpl
import com.emm.mybest.data.HabitRepositoryImpl
import com.emm.mybest.data.PhotoRepositoryImpl
import com.emm.mybest.data.UserPreferencesRepositoryImpl
import com.emm.mybest.data.WeightRepositoryImpl
import com.emm.mybest.domain.media.MediaManager
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.HabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.CreateHabitUseCase
import com.emm.mybest.domain.usecase.GetDailyHabitsUseCase
import com.emm.mybest.domain.usecase.GetHomeSummaryUseCase
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.domain.usecase.ToggleHabitUseCase
import com.emm.mybest.features.habit.presentation.AddHabitViewModel
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.home.presentation.HomeViewModel
import com.emm.mybest.features.insights.presentation.InsightsViewModel
import com.emm.mybest.features.photo.presentation.AddPhotoViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.timeline.presentation.TimelineViewModel
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import com.emm.mybest.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "my_best_db",
        ).fallbackToDestructiveMigration(false).build()
    }

    single { get<AppDatabase>().dailyHabitDao() }
    single { get<AppDatabase>().dailyWeightDao() }
    single { get<AppDatabase>().progressPhotoDao() }
    single { get<AppDatabase>().habitDao() }
    single { get<AppDatabase>().habitRecordDao() }
    single<HabitRepository> { HabitRepositoryImpl(get(), get()) }
    single<WeightRepository> { WeightRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }

    single<DailyHabitRepository> { DailyHabitRepositoryImpl(get()) }

    factory { CreateHabitUseCase(get()) }
    factory { GetDailyHabitsUseCase(get()) }
    factory { ToggleHabitUseCase(get()) }
    factory { GetHomeSummaryUseCase(get(), get(), get()) }
    factory { GetInsightsUseCase(get(), get(), get()) }

    single { MediaManager(androidContext()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { AddWeightViewModel(get()) }
    viewModel { AddHabitViewModel(get()) }
    viewModel { AddPhotoViewModel(get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { InsightsViewModel(get()) }
    viewModel { ComparePhotosViewModel(get()) }
    viewModel { TimelineViewModel(get()) }
    viewModel { MainViewModel(get()) }
}
