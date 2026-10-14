package com.mediguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.AppNavHost
import com.mediguard.app.ui.theme.MediGuardTheme

class MainActivity : ComponentActivity() {
    private val repository = MediGuardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(repository = repository)
                }
            }
        }
    }
}
