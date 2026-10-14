package com.mediguard.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.screens.auth.*
import com.mediguard.app.ui.screens.caregiver.JoinRoomScreen
import com.mediguard.app.ui.screens.common.MainScreen
import com.mediguard.app.ui.screens.common.NotificationsScreen

@Composable
fun AppNavHost(
    repository: MediGuardRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Splash.route
    ) {
        composable(NavRoute.Splash.route) {
            SplashScreen(
                repository = repository,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoute.Onboarding.route) {
            OnboardingScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(NavRoute.Login.route) {
            LoginScreen(
                repository = repository,
                onNavigate = { route ->
                    if (route == NavRoute.Main.route) {
                        navController.navigate(route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(NavRoute.Register.route) {
            RegisterScreen(
                repository = repository,
                onNavigate = { route ->
                    if (route == NavRoute.Main.route) {
                        navController.navigate(route) {
                            popUpTo(NavRoute.Register.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(NavRoute.ForgotPassword.route) {
            ForgotPasswordScreen(
                repository = repository,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(NavRoute.Main.route) {
            MainScreen(
                repository = repository,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(NavRoute.Notifications.route) {
            NotificationsScreen(repository = repository)
        }
        composable(NavRoute.JoinRoom.route) {
            JoinRoomScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
