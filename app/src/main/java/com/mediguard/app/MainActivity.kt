package com.mediguard.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.mediguard.app.data.model.ThemeMode
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.notification.NotificationHelper
import com.mediguard.app.ui.navigation.AppNavHost
import com.mediguard.app.ui.theme.AppBackground
import com.mediguard.app.ui.theme.MediGuardTheme

class MainActivity : ComponentActivity() {
    private val repository = MediGuardRepository()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            repository.rescheduleUpcomingAlarms()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channel for medication reminders
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        repository.initPreferences(this)
        handleNotificationIntent(intent)
        setContent {
            val themeMode by repository.themeMode.collectAsState()
            val colorBlindMode by repository.colorBlindMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MediGuardTheme(
                darkTheme = isDark,
                colorBlindMode = colorBlindMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackground
                ) {
                    AppNavHost(repository = repository)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val doseId = intent?.getStringExtra("EXTRA_DOSE_EVENT_ID")
        if (!doseId.isNullOrBlank()) {
            repository.setPendingDoseActionId(doseId)
        }
    }
}
