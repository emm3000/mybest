package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainState(
    val isDarkMode: Boolean? = null,
    val useDynamicColor: Boolean = true
)

class MainViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val state: StateFlow<MainState> = combine(
        preferencesRepository.isDarkMode,
        preferencesRepository.useDynamicColor
    ) { isDarkMode, useDynamicColor ->
        MainState(
            isDarkMode = isDarkMode,
            useDynamicColor = useDynamicColor
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT),
            initialValue = MainState()
        )

    companion object {
        private const val STATE_FLOW_STOP_TIMEOUT = 5000L
    }
}
