package com.mediguard.app.ui.navigation

sealed class NavRoute(val route: String) {
    object Splash : NavRoute("splash")
    object Onboarding : NavRoute("onboarding")
    object Login : NavRoute("login")
    object Register : NavRoute("register")
    object ForgotPassword : NavRoute("forgot_password")
    object Main : NavRoute("main")
    object Notifications : NavRoute("notifications")
    object JoinRoom : NavRoute("join_room")
}
