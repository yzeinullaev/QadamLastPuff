package com.qadam.lastpuff.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qadam.lastpuff.ui.screens.achievements.AchievementsScreen
import com.qadam.lastpuff.ui.screens.body.BodyScreen
import com.qadam.lastpuff.ui.screens.home.HomeScreen
import com.qadam.lastpuff.ui.screens.money.MoneyScreen
import com.qadam.lastpuff.ui.screens.onboarding.OnboardingScreen
import com.qadam.lastpuff.ui.screens.profile.ProfileScreen
import com.qadam.lastpuff.ui.screens.progress.ProgressScreen
import com.qadam.lastpuff.ui.screens.sos.SosScreen
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.util.RecoveryCalculator

@Composable
fun QadamNavGraph(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val canShowMainApp = onboardingCompleted && profile != null
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = canShowMainApp &&
        currentRoute != Screen.Onboarding.route &&
        currentRoute != Screen.Sos.route

    LaunchedEffect(canShowMainApp) {
        if (canShowMainApp && currentRoute == Screen.Onboarding.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (canShowMainApp) Screen.Home.route else Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(viewModel = viewModel)
        }

        composable(Screen.Home.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                HomeScreen(
                    viewModel = viewModel,
                    onSosClick = {
                        viewModel.startSos()
                        navController.navigate(Screen.Sos.route)
                    },
                    onEmergencySosClick = {
                        viewModel.startEmergencySos()
                        navController.navigate(Screen.Sos.route)
                    }
                )
            }
        }

        composable(Screen.Progress.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                ProgressScreen(viewModel)
            }
        }

        composable(Screen.Money.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                MoneyScreen(viewModel)
            }
        }

        composable(Screen.Body.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                val recoveryIndex by viewModel.recoveryIndex.collectAsState()
                BodyScreen(
                    organs = RecoveryCalculator.toOrgans(recoveryIndex),
                    willpower = recoveryIndex.willpower
                )
            }
        }

        composable(Screen.Achievements.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                AchievementsScreen(viewModel)
            }
        }

        composable(Screen.Profile.route) {
            MainScaffold(currentRoute, showBottomBar, navigateToTab) {
                ProfileScreen(viewModel)
            }
        }

        composable(Screen.Sos.route) {
            SosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainScaffold(
    currentRoute: String?,
    showBottomBar: Boolean,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(item.screen.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            alwaysShowLabel = false,
                            label = {}
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
