package com.mediguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mediguard.app.data.model.ThemeMode
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.AppNavHost
import com.mediguard.app.ui.theme.AppBackground
import com.mediguard.app.ui.theme.MediGuardTheme

class MainActivity : ComponentActivity() {
    private val repository = MediGuardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository.initPreferences(this)
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
}
