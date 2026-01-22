package com.emm.mybest.di

import androidx.room.Room
import com.emm.mybest.data.AppDatabase
import com.emm.mybest.viewmodel.AddHabitViewModel
import com.emm.mybest.viewmodel.AddWeightViewModel
import com.emm.mybest.viewmodel.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "my_best_db"
        ).build()
    }
    
    single { get<AppDatabase>().dailyHabitDao() }
    single { get<AppDatabase>().dailyWeightDao() }
    single { get<AppDatabase>().progressPhotoDao() }
    
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { AddWeightViewModel(get()) }
    viewModel { AddHabitViewModel(get()) }
}
