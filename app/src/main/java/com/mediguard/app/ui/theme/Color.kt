package com.mediguard.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Healthcare Brand Colors (Deep Medical Teal & Clean Cyan)
val TealPrimary = Color(0xFF0F766E)
val TealPrimaryDark = Color(0xFF115E59)
val TealPrimaryLight = Color(0xFF14B8A6)
val TealContainer = Color(0xFFE6F4F1)
val OnTealContainer = Color(0xFF0F766E)

// Secondary & Accent Colors
val AccentCyan = Color(0xFF0284C7)
val AccentCyanContainer = Color(0xFFE0F2FE)
val AccentIndigo = Color(0xFF4F46E5)

// Surfaces & Backgrounds
val AppBackground = Color(0xFFF8FAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFF1F5F9)
val SurfaceBorder = Color(0xFFE2E8F0)
val SurfaceBorderFocus = Color(0xFF0F766E)

// Typography & Contrast
val TextDark = Color(0xFF0F172A)
val TextMuted = Color(0xFF64748B)
val TextLight = Color(0xFF94A3B8)

// Status & Semantic Adherence Colors
val StatusScheduled = Color(0xFF64748B)
val StatusPending = Color(0xFFD97706)
val StatusTaken = Color(0xFF0284C7)
val StatusVerified = Color(0xFF059669)
val StatusMissed = Color(0xFFDC2626)
val StatusLate = Color(0xFFEA580C)

// Soft Status Badge Containers & Text
val StatusPendingBg = Color(0xFFFEF3C7)
val StatusPendingText = Color(0xFF92400E)
val StatusTakenBg = Color(0xFFDBEAFE)
val StatusTakenText = Color(0xFF1E40AF)
val StatusVerifiedBg = Color(0xFFDCFCE7)
val StatusVerifiedText = Color(0xFF166534)
val StatusMissedBg = Color(0xFFFEE2E2)
val StatusMissedText = Color(0xFF991B1B)
val StatusLateBg = Color(0xFFFFEDD5)
val StatusLateText = Color(0xFF9A3412)

// Premium Human-Crafted Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0B5563), Color(0xFF1A8A7B))
)
val CardHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0F766E), Color(0xFF0E7490))
)
val SoftCyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE6F4F1), Color(0xFFE0F2FE))
)

// Legacy compatibility aliases
val BluePrimary = TealPrimary
val BlueSecondary = AccentCyan
val BlueBackground = AppBackground
val CardBackground = SurfaceCard
val TextPrimary = TextDark
val TextSecondary = TextMuted
