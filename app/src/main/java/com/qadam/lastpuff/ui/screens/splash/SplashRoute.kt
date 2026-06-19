package com.qadam.lastpuff.ui.screens.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.qadam.lastpuff.ui.viewmodel.SplashViewModel

@Composable
fun SplashRoute(
    viewModel: SplashViewModel,
    onFinished: () -> Unit
) {
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val recoveryIndex by viewModel.recoveryIndex.collectAsState()

    val showFirstLaunchIntro = isFirstLaunch && !onboardingCompleted

    LaunchedEffect(isFirstLaunch, onboardingCompleted) {
        if (isFirstLaunch && onboardingCompleted) {
            viewModel.completeFirstLaunch()
        }
    }

    if (showFirstLaunchIntro) {
        FirstLaunchIntroScreen(
            onFinished = {
                viewModel.completeFirstLaunch()
                onFinished()
            }
        )
    } else {
        RegularSplashScreen(
            recoveryIndex = recoveryIndex,
            onFinished = onFinished
        )
    }
}
