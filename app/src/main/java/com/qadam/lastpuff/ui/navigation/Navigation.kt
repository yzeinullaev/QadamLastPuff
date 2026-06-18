package com.qadam.lastpuff.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Progress : Screen("progress")
    data object Money : Screen("money")
    data object Body : Screen("body")
    data object Achievements : Screen("achievements")
    data object Profile : Screen("profile")
    data object Sos : Screen("sos")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Главная", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Progress, "Прогресс", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
    BottomNavItem(Screen.Money, "Цель", Icons.Filled.Savings, Icons.Outlined.Savings),
    BottomNavItem(Screen.Body, "Тело", Icons.Filled.AccessibilityNew, Icons.Outlined.AccessibilityNew),
    BottomNavItem(Screen.Achievements, "Награды", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem(Screen.Profile, "Профиль", Icons.Filled.Person, Icons.Outlined.Person)
)
