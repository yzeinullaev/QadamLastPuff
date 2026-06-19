package com.qadam.lastpuff.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qadam.lastpuff.data.repository.UserRepository
import com.qadam.lastpuff.domain.model.RecoveryIndex
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: UserRepository) : ViewModel() {

    val isFirstLaunch = repository.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val onboardingCompleted = repository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recoveryIndex = repository.observeRecoveryIndex()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecoveryIndex())

    init {
        viewModelScope.launch { repository.syncLaunchState() }
    }

    fun completeFirstLaunch() {
        viewModelScope.launch {
            repository.setFirstLaunchComplete()
        }
    }
}

class SplashViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
