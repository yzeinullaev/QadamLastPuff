package com.qadam.lastpuff

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qadam.lastpuff.ui.navigation.QadamNavGraph
import com.qadam.lastpuff.ui.screens.splash.SplashRoute
import com.qadam.lastpuff.ui.theme.QadamTheme
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.ui.viewmodel.AppViewModelFactory
import com.qadam.lastpuff.ui.viewmodel.SplashViewModel
import com.qadam.lastpuff.ui.viewmodel.SplashViewModelFactory
import com.qadam.lastpuff.worker.NotificationScheduler

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        val app = application as QadamApplication
        val factory = AppViewModelFactory(app.repository)

        setContent {
            val viewModel: AppViewModel = viewModel(factory = factory)
            val darkThemePref by viewModel.darkTheme.collectAsState()
            val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
            val notificationHour by viewModel.notificationHour.collectAsState()
            val notificationMinute by viewModel.notificationMinute.collectAsState()

            val useDarkTheme = darkThemePref ?: isSystemInDarkTheme()

            if (notificationsEnabled) {
                NotificationScheduler.scheduleDaily(
                    context = this,
                    hour = notificationHour,
                    minute = notificationMinute
                )
            } else {
                NotificationScheduler.cancel(this)
            }

            QadamTheme(darkTheme = useDarkTheme) {
                val splashViewModel: SplashViewModel = viewModel(factory = SplashViewModelFactory(app.repository))
                var splashDone by remember { mutableStateOf(false) }

                if (!splashDone) {
                    SplashRoute(
                        viewModel = splashViewModel,
                        onFinished = { splashDone = true }
                    )
                } else {
                    QadamNavGraph(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
