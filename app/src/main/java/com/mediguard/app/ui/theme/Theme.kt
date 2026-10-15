package com.mediguard.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mediguard.app.data.model.ColorBlindMode

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MediGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorBlindMode: ColorBlindMode = ColorBlindMode.OFF,
    content: @Composable () -> Unit
) {
    val mediGuardColors = remember(darkTheme, colorBlindMode) {
        resolveMediGuardColors(isDark = darkTheme, mode = colorBlindMode)
    }

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = mediGuardColors.primary,
            onPrimary = Color(0xFF042F2E),
            primaryContainer = mediGuardColors.primaryContainer,
            onPrimaryContainer = mediGuardColors.onPrimaryContainer,
            secondary = mediGuardColors.accentCyan,
            onSecondary = Color(0xFF082F49),
            tertiary = mediGuardColors.accentIndigo,
            onTertiary = Color.White,
            background = mediGuardColors.background,
            onBackground = mediGuardColors.textDark,
            surface = mediGuardColors.surface,
            onSurface = mediGuardColors.textDark,
            surfaceVariant = mediGuardColors.surfaceSubtle,
            onSurfaceVariant = mediGuardColors.textMuted,
            outline = mediGuardColors.surfaceBorder,
            outlineVariant = mediGuardColors.surfaceSubtle
        )
    } else {
        lightColorScheme(
            primary = mediGuardColors.primary,
            onPrimary = Color.White,
            primaryContainer = mediGuardColors.primaryContainer,
            onPrimaryContainer = mediGuardColors.onPrimaryContainer,
            secondary = mediGuardColors.accentCyan,
            onSecondary = Color.White,
            secondaryContainer = mediGuardColors.accentCyanContainer,
            onSecondaryContainer = Color(0xFF0369A1),
            tertiary = mediGuardColors.accentIndigo,
            onTertiary = Color.White,
            background = mediGuardColors.background,
            onBackground = mediGuardColors.textDark,
            surface = mediGuardColors.surface,
            onSurface = mediGuardColors.textDark,
            surfaceVariant = mediGuardColors.surfaceSubtle,
            onSurfaceVariant = mediGuardColors.textMuted,
            outline = mediGuardColors.surfaceBorder,
            outlineVariant = Color(0xFFCBD5E1)
        )
    }

    CompositionLocalProvider(
        LocalMediGuardColors provides mediGuardColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
