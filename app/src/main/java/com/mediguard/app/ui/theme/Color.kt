package com.mediguard.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mediguard.app.data.model.ColorBlindMode

data class MediGuardColors(
    val isDark: Boolean = false,
    val colorBlindMode: ColorBlindMode = ColorBlindMode.OFF,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val accentCyan: Color,
    val accentCyanContainer: Color,
    val accentIndigo: Color,
    val background: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceBorder: Color,
    val surfaceBorderFocus: Color,
    val textDark: Color,
    val textMuted: Color,
    val textLight: Color,
    val statusScheduled: Color,
    val statusPending: Color,
    val statusTaken: Color,
    val statusVerified: Color,
    val statusMissed: Color,
    val statusLate: Color,
    val statusPendingBg: Color,
    val statusPendingText: Color,
    val statusTakenBg: Color,
    val statusTakenText: Color,
    val statusVerifiedBg: Color,
    val statusVerifiedText: Color,
    val statusMissedBg: Color,
    val statusMissedText: Color,
    val statusLateBg: Color,
    val statusLateText: Color,
    val cardHeroGradient: Brush,
    val primaryGradient: Brush,
    val softCyanGradient: Brush
)

fun resolveMediGuardColors(isDark: Boolean, mode: ColorBlindMode): MediGuardColors {
    return if (!isDark) {
        when (mode) {
            ColorBlindMode.OFF -> MediGuardColors(
                isDark = false,
                colorBlindMode = mode,
                primary = Color(0xFF0F766E),
                primaryDark = Color(0xFF115E59),
                primaryLight = Color(0xFF14B8A6),
                primaryContainer = Color(0xFFE6F4F1),
                onPrimaryContainer = Color(0xFF0F766E),
                accentCyan = Color(0xFF0284C7),
                accentCyanContainer = Color(0xFFE0F2FE),
                accentIndigo = Color(0xFF4F46E5),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceSubtle = Color(0xFFF1F5F9),
                surfaceBorder = Color(0xFFE2E8F0),
                surfaceBorderFocus = Color(0xFF0F766E),
                textDark = Color(0xFF0F172A),
                textMuted = Color(0xFF64748B),
                textLight = Color(0xFF94A3B8),
                statusScheduled = Color(0xFF64748B),
                statusPending = Color(0xFFD97706),
                statusTaken = Color(0xFF0284C7),
                statusVerified = Color(0xFF059669),
                statusMissed = Color(0xFFDC2626),
                statusLate = Color(0xFFEA580C),
                statusPendingBg = Color(0xFFFEF3C7),
                statusPendingText = Color(0xFF92400E),
                statusTakenBg = Color(0xFFDBEAFE),
                statusTakenText = Color(0xFF1E40AF),
                statusVerifiedBg = Color(0xFFDCFCE7),
                statusVerifiedText = Color(0xFF166534),
                statusMissedBg = Color(0xFFFEE2E2),
                statusMissedText = Color(0xFF991B1B),
                statusLateBg = Color(0xFFFFEDD5),
                statusLateText = Color(0xFF9A3412),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0E7490))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF0B5563), Color(0xFF1A8A7B))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFFE6F4F1), Color(0xFFE0F2FE)))
            )
            ColorBlindMode.PROTANOPIA_DEUTERANOPIA -> MediGuardColors(
                isDark = false,
                colorBlindMode = mode,
                primary = Color(0xFF0F766E),
                primaryDark = Color(0xFF115E59),
                primaryLight = Color(0xFF14B8A6),
                primaryContainer = Color(0xFFE6F4F1),
                onPrimaryContainer = Color(0xFF0F766E),
                accentCyan = Color(0xFF0284C7),
                accentCyanContainer = Color(0xFFE0F2FE),
                accentIndigo = Color(0xFF4F46E5),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceSubtle = Color(0xFFF1F5F9),
                surfaceBorder = Color(0xFFCBD5E1),
                surfaceBorderFocus = Color(0xFF1D4ED8),
                textDark = Color(0xFF0F172A),
                textMuted = Color(0xFF475569),
                textLight = Color(0xFF64748B),
                statusScheduled = Color(0xFF64748B),
                statusPending = Color(0xFFD97706),
                statusTaken = Color(0xFF0284C7),
                // Replace green with royal blue for verified
                statusVerified = Color(0xFF1D4ED8),
                // Replace red with vivid magenta for missed
                statusMissed = Color(0xFFC026D3),
                statusLate = Color(0xFFE11D48),
                statusPendingBg = Color(0xFFFEF3C7),
                statusPendingText = Color(0xFF92400E),
                statusTakenBg = Color(0xFFE0F2FE),
                statusTakenText = Color(0xFF0369A1),
                statusVerifiedBg = Color(0xFFDBEAFE),
                statusVerifiedText = Color(0xFF1E40AF),
                statusMissedBg = Color(0xFFFAE8FF),
                statusMissedText = Color(0xFF86198F),
                statusLateBg = Color(0xFFFFE4E6),
                statusLateText = Color(0xFF9F1239),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF1D4ED8))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF2563EB))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFFE6F4F1), Color(0xFFDBEAFE)))
            )
            ColorBlindMode.TRITANOPIA -> MediGuardColors(
                isDark = false,
                colorBlindMode = mode,
                primary = Color(0xFF0F766E),
                primaryDark = Color(0xFF115E59),
                primaryLight = Color(0xFF14B8A6),
                primaryContainer = Color(0xFFE6F4F1),
                onPrimaryContainer = Color(0xFF0F766E),
                accentCyan = Color(0xFF0891B2),
                accentCyanContainer = Color(0xFFCFFAFE),
                accentIndigo = Color(0xFF7C3AED),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceSubtle = Color(0xFFF1F5F9),
                surfaceBorder = Color(0xFFCBD5E1),
                surfaceBorderFocus = Color(0xFF0F766E),
                textDark = Color(0xFF0F172A),
                textMuted = Color(0xFF475569),
                textLight = Color(0xFF64748B),
                statusScheduled = Color(0xFF64748B),
                // Replace yellow with violet/indigo to avoid blue-yellow confusion
                statusPending = Color(0xFF7C3AED),
                statusTaken = Color(0xFF0D9488),
                statusVerified = Color(0xFF059669),
                statusMissed = Color(0xFFDC2626),
                statusLate = Color(0xFF9333EA),
                statusPendingBg = Color(0xFFEDE9FE),
                statusPendingText = Color(0xFF5B21B6),
                statusTakenBg = Color(0xFFCCFBF1),
                statusTakenText = Color(0xFF115E59),
                statusVerifiedBg = Color(0xFFDCFCE7),
                statusVerifiedText = Color(0xFF166534),
                statusMissedBg = Color(0xFFFEE2E2),
                statusMissedText = Color(0xFF991B1B),
                statusLateBg = Color(0xFFF3E8FF),
                statusLateText = Color(0xFF6B21A8),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF047857))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF059669))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFFE6F4F1), Color(0xFFEDE9FE)))
            )
            ColorBlindMode.HIGH_CONTRAST -> MediGuardColors(
                isDark = false,
                colorBlindMode = mode,
                primary = Color(0xFF004D40),
                primaryDark = Color(0xFF00251A),
                primaryLight = Color(0xFF00796B),
                primaryContainer = Color(0xFFB2DFDB),
                onPrimaryContainer = Color(0xFF00251A),
                accentCyan = Color(0xFF0D47A1),
                accentCyanContainer = Color(0xFFBBDEFB),
                accentIndigo = Color(0xFF311B92),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFFFFFFF),
                surfaceSubtle = Color(0xFFF3F4F6),
                surfaceBorder = Color(0xFF000000),
                surfaceBorderFocus = Color(0xFF004D40),
                textDark = Color(0xFF000000),
                textMuted = Color(0xFF1F2937),
                textLight = Color(0xFF374151),
                statusScheduled = Color(0xFF1F2937),
                statusPending = Color(0xFFB45309),
                statusTaken = Color(0xFF0D47A1),
                statusVerified = Color(0xFF00695C),
                statusMissed = Color(0xFFB71C1C),
                statusLate = Color(0xFFC2410C),
                statusPendingBg = Color(0xFFFEF3C7),
                statusPendingText = Color(0xFF78350F),
                statusTakenBg = Color(0xFFDBEAFE),
                statusTakenText = Color(0xFF1E3A8A),
                statusVerifiedBg = Color(0xFFDCFCE7),
                statusVerifiedText = Color(0xFF064E3B),
                statusMissedBg = Color(0xFFFEE2E2),
                statusMissedText = Color(0xFF7F1D1D),
                statusLateBg = Color(0xFFFFEDD5),
                statusLateText = Color(0xFF7C2D12),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF004D40), Color(0xFF00251A))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF004D40), Color(0xFF00695C))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)))
            )
        }
    } else {
        // Dark Mode
        when (mode) {
            ColorBlindMode.OFF -> MediGuardColors(
                isDark = true,
                colorBlindMode = mode,
                primary = Color(0xFF14B8A6),
                primaryDark = Color(0xFF0F766E),
                primaryLight = Color(0xFF2DD4BF),
                primaryContainer = Color(0xFF134E4A),
                onPrimaryContainer = Color(0xFFCCFBF1),
                accentCyan = Color(0xFF38BDF8),
                accentCyanContainer = Color(0xFF075985),
                accentIndigo = Color(0xFF818CF8),
                background = Color(0xFF0B1320),
                surface = Color(0xFF1E293B),
                surfaceSubtle = Color(0xFF273549),
                surfaceBorder = Color(0xFF334155),
                surfaceBorderFocus = Color(0xFF2DD4BF),
                textDark = Color(0xFFF8FAFC),
                textMuted = Color(0xFF94A3B8),
                textLight = Color(0xFF64748B),
                statusScheduled = Color(0xFF94A3B8),
                statusPending = Color(0xFFFBBF24),
                statusTaken = Color(0xFF38BDF8),
                statusVerified = Color(0xFF34D399),
                statusMissed = Color(0xFFF87171),
                statusLate = Color(0xFFFB923C),
                statusPendingBg = Color(0xFF451A03),
                statusPendingText = Color(0xFFFDE68A),
                statusTakenBg = Color(0xFF082F49),
                statusTakenText = Color(0xFFBAE6FD),
                statusVerifiedBg = Color(0xFF064E3B),
                statusVerifiedText = Color(0xFFA7F3D0),
                statusMissedBg = Color(0xFF450A0A),
                statusMissedText = Color(0xFFFECACA),
                statusLateBg = Color(0xFF431407),
                statusLateText = Color(0xFFFFEDD5),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF0E7490))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF115E59), Color(0xFF0F766E))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF0C4A6E)))
            )
            ColorBlindMode.PROTANOPIA_DEUTERANOPIA -> MediGuardColors(
                isDark = true,
                colorBlindMode = mode,
                primary = Color(0xFF14B8A6),
                primaryDark = Color(0xFF0F766E),
                primaryLight = Color(0xFF2DD4BF),
                primaryContainer = Color(0xFF134E4A),
                onPrimaryContainer = Color(0xFFCCFBF1),
                accentCyan = Color(0xFF38BDF8),
                accentCyanContainer = Color(0xFF075985),
                accentIndigo = Color(0xFF818CF8),
                background = Color(0xFF0B1320),
                surface = Color(0xFF1E293B),
                surfaceSubtle = Color(0xFF273549),
                surfaceBorder = Color(0xFF3B4D66),
                surfaceBorderFocus = Color(0xFF60A5FA),
                textDark = Color(0xFFF8FAFC),
                textMuted = Color(0xFF94A3B8),
                textLight = Color(0xFF64748B),
                statusScheduled = Color(0xFF94A3B8),
                statusPending = Color(0xFFFBBF24),
                statusTaken = Color(0xFF38BDF8),
                // Replace green with bright blue in dark mode
                statusVerified = Color(0xFF60A5FA),
                // Replace red with vivid magenta in dark mode
                statusMissed = Color(0xFFE879F9),
                statusLate = Color(0xFFFB7185),
                statusPendingBg = Color(0xFF451A03),
                statusPendingText = Color(0xFFFDE68A),
                statusTakenBg = Color(0xFF082F49),
                statusTakenText = Color(0xFFBAE6FD),
                statusVerifiedBg = Color(0xFF1E3A8A),
                statusVerifiedText = Color(0xFFDBEAFE),
                statusMissedBg = Color(0xFF701A75),
                statusMissedText = Color(0xFFF5D0FE),
                statusLateBg = Color(0xFF881337),
                statusLateText = Color(0xFFFFE4E6),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF1E3A8A))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF115E59), Color(0xFF1D4ED8))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF1E3A8A)))
            )
            ColorBlindMode.TRITANOPIA -> MediGuardColors(
                isDark = true,
                colorBlindMode = mode,
                primary = Color(0xFF14B8A6),
                primaryDark = Color(0xFF0F766E),
                primaryLight = Color(0xFF2DD4BF),
                primaryContainer = Color(0xFF134E4A),
                onPrimaryContainer = Color(0xFFCCFBF1),
                accentCyan = Color(0xFF06B6D4),
                accentCyanContainer = Color(0xFF164E63),
                accentIndigo = Color(0xFFA78BFA),
                background = Color(0xFF0B1320),
                surface = Color(0xFF1E293B),
                surfaceSubtle = Color(0xFF273549),
                surfaceBorder = Color(0xFF334155),
                surfaceBorderFocus = Color(0xFF14B8A6),
                textDark = Color(0xFFF8FAFC),
                textMuted = Color(0xFF94A3B8),
                textLight = Color(0xFF64748B),
                statusScheduled = Color(0xFF94A3B8),
                statusPending = Color(0xFFA78BFA),
                statusTaken = Color(0xFF2DD4BF),
                statusVerified = Color(0xFF34D399),
                statusMissed = Color(0xFFF87171),
                statusLate = Color(0xFFC084FC),
                statusPendingBg = Color(0xFF4C1D95),
                statusPendingText = Color(0xFFEDE9FE),
                statusTakenBg = Color(0xFF134E4A),
                statusTakenText = Color(0xFF99F6E4),
                statusVerifiedBg = Color(0xFF064E3B),
                statusVerifiedText = Color(0xFFA7F3D0),
                statusMissedBg = Color(0xFF450A0A),
                statusMissedText = Color(0xFFFECACA),
                statusLateBg = Color(0xFF581C87),
                statusLateText = Color(0xFFF3E8FF),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF065F46))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF115E59), Color(0xFF059669))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFF134E4A), Color(0xFF4C1D95)))
            )
            ColorBlindMode.HIGH_CONTRAST -> MediGuardColors(
                isDark = true,
                colorBlindMode = mode,
                primary = Color(0xFF2DD4BF),
                primaryDark = Color(0xFF14B8A6),
                primaryLight = Color(0xFF5EEAD4),
                primaryContainer = Color(0xFF115E59),
                onPrimaryContainer = Color(0xFFFFFFFF),
                accentCyan = Color(0xFF60A5FA),
                accentCyanContainer = Color(0xFF1E3A8A),
                accentIndigo = Color(0xFFA5B4FC),
                background = Color(0xFF000000),
                surface = Color(0xFF0F172A),
                surfaceSubtle = Color(0xFF1E293B),
                surfaceBorder = Color(0xFFFFFFFF),
                surfaceBorderFocus = Color(0xFF2DD4BF),
                textDark = Color(0xFFFFFFFF),
                textMuted = Color(0xFFE2E8F0),
                textLight = Color(0xFFCBD5E1),
                statusScheduled = Color(0xFFCBD5E1),
                statusPending = Color(0xFFFCD34D),
                statusTaken = Color(0xFF60A5FA),
                statusVerified = Color(0xFF4ADE80),
                statusMissed = Color(0xFFF87171),
                statusLate = Color(0xFFFB923C),
                statusPendingBg = Color(0xFF78350F),
                statusPendingText = Color(0xFFFEF3C7),
                statusTakenBg = Color(0xFF1E3A8A),
                statusTakenText = Color(0xFFDBEAFE),
                statusVerifiedBg = Color(0xFF064E3B),
                statusVerifiedText = Color(0xFFDCFCE7),
                statusMissedBg = Color(0xFF7F1D1D),
                statusMissedText = Color(0xFFFEE2E2),
                statusLateBg = Color(0xFF7C2D12),
                statusLateText = Color(0xFFFFEDD5),
                cardHeroGradient = Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFF134E4A))),
                primaryGradient = Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFF0F766E))),
                softCyanGradient = Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFF1E293B)))
            )
        }
    }
}

val LocalMediGuardColors = staticCompositionLocalOf {
    resolveMediGuardColors(isDark = false, mode = ColorBlindMode.OFF)
}

// Dynamic Top-level Composable Accessors
val TealPrimary: Color @Composable get() = LocalMediGuardColors.current.primary
val TealPrimaryDark: Color @Composable get() = LocalMediGuardColors.current.primaryDark
val TealPrimaryLight: Color @Composable get() = LocalMediGuardColors.current.primaryLight
val TealContainer: Color @Composable get() = LocalMediGuardColors.current.primaryContainer
val OnTealContainer: Color @Composable get() = LocalMediGuardColors.current.onPrimaryContainer

val AccentCyan: Color @Composable get() = LocalMediGuardColors.current.accentCyan
val AccentCyanContainer: Color @Composable get() = LocalMediGuardColors.current.accentCyanContainer
val AccentIndigo: Color @Composable get() = LocalMediGuardColors.current.accentIndigo

val AppBackground: Color @Composable get() = LocalMediGuardColors.current.background
val SurfaceCard: Color @Composable get() = LocalMediGuardColors.current.surface
val SurfaceSubtle: Color @Composable get() = LocalMediGuardColors.current.surfaceSubtle
val SurfaceBorder: Color @Composable get() = LocalMediGuardColors.current.surfaceBorder
val SurfaceBorderFocus: Color @Composable get() = LocalMediGuardColors.current.surfaceBorderFocus

val TextDark: Color @Composable get() = LocalMediGuardColors.current.textDark
val TextMuted: Color @Composable get() = LocalMediGuardColors.current.textMuted
val TextLight: Color @Composable get() = LocalMediGuardColors.current.textLight

val StatusScheduled: Color @Composable get() = LocalMediGuardColors.current.statusScheduled
val StatusPending: Color @Composable get() = LocalMediGuardColors.current.statusPending
val StatusTaken: Color @Composable get() = LocalMediGuardColors.current.statusTaken
val StatusVerified: Color @Composable get() = LocalMediGuardColors.current.statusVerified
val StatusMissed: Color @Composable get() = LocalMediGuardColors.current.statusMissed
val StatusLate: Color @Composable get() = LocalMediGuardColors.current.statusLate

val StatusPendingBg: Color @Composable get() = LocalMediGuardColors.current.statusPendingBg
val StatusPendingText: Color @Composable get() = LocalMediGuardColors.current.statusPendingText
val StatusTakenBg: Color @Composable get() = LocalMediGuardColors.current.statusTakenBg
val StatusTakenText: Color @Composable get() = LocalMediGuardColors.current.statusTakenText
val StatusVerifiedBg: Color @Composable get() = LocalMediGuardColors.current.statusVerifiedBg
val StatusVerifiedText: Color @Composable get() = LocalMediGuardColors.current.statusVerifiedText
val StatusMissedBg: Color @Composable get() = LocalMediGuardColors.current.statusMissedBg
val StatusMissedText: Color @Composable get() = LocalMediGuardColors.current.statusMissedText
val StatusLateBg: Color @Composable get() = LocalMediGuardColors.current.statusLateBg
val StatusLateText: Color @Composable get() = LocalMediGuardColors.current.statusLateText

val PrimaryGradient: Brush @Composable get() = LocalMediGuardColors.current.primaryGradient
val CardHeroGradient: Brush @Composable get() = LocalMediGuardColors.current.cardHeroGradient
val SoftCyanGradient: Brush @Composable get() = LocalMediGuardColors.current.softCyanGradient

// Legacy compatibility aliases
val BluePrimary: Color @Composable get() = TealPrimary
val BlueSecondary: Color @Composable get() = AccentCyan
val BlueBackground: Color @Composable get() = AppBackground
val CardBackground: Color @Composable get() = SurfaceCard
val TextPrimary: Color @Composable get() = TextDark
val TextSecondary: Color @Composable get() = TextMuted
