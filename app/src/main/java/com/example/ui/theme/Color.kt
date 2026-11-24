package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Classic dark studio tokens (used by Editor canvas & dark screens)
val BackgroundDark = Color(0xFF121112)
val SurfaceDark = Color(0xFF181618)
val SurfaceCard = Color(0xFF1D1B1D)
val SurfaceElevated = Color(0xFF282528)
val SurfacePill = Color(0xFF2A272A)
val SurfacePillActive = Color(0xFF423F42)
val SurfaceBright = Color(0xFFFFFFFF)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB5B0B5)
val TextMuted = Color(0xFF8C888C)
val TextOnWhite = Color(0xFF121112)

val BadgeHot = Color(0xFFFF6B4A)
val BadgeNew = Color(0xFFFF5CA8)
val BadgePro = Color(0xFFE2C275)
val AccentTeal = Color(0xFF4ADBC8)
val AccentPurple = Color(0xFFB57BFF)
val ErrorRed = Color(0xFFFF5A5F)

val BorderSubtle = Color(0x14FFFFFF)
val BorderMedium = Color(0x28FFFFFF)

// Adaptive White & Black Theme tokens matching Studio Profile & Drafts UI
data class StudioColorPalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundGlow: Color,
    val card: Color,
    val cardSecondary: Color,
    val glassStart: Color,
    val glassEnd: Color,
    val pillBg: Color,
    val pillText: Color,
    val sheetBg: Color,
    val cancelBg: Color,
    val navBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val accent: Color,
    val danger: Color,
    val contrastSurface: Color,
    val onContrastSurface: Color
)

val LightStudioColors = StudioColorPalette(
    isDark = false,
    background = Color(0xFFF4F5F9),
    backgroundGlow = Color(0xFFE4E7F5),
    card = Color(0xFFFFFFFF),
    cardSecondary = Color(0xFFEEF0F6),
    glassStart = Color(0xFFFFFFFF),
    glassEnd = Color(0xFFEBEDF7),
    pillBg = Color(0xFF14161F),
    pillText = Color(0xFFFFFFFF),
    sheetBg = Color(0xFFFFFFFF),
    cancelBg = Color(0xFFECEEF5),
    navBg = Color(0xF5FFFFFF),
    textPrimary = Color(0xFF12141C),
    textSecondary = Color(0xFF484C5E),
    textMuted = Color(0xFF656A7C),
    border = Color(0x1712141C),
    accent = Color(0xFF6C47FF),
    danger = Color(0xFFE63950),
    contrastSurface = Color(0xFF14161F),
    onContrastSurface = Color(0xFFFFFFFF)
)

val DarkStudioColors = StudioColorPalette(
    isDark = true,
    background = Color(0xFF0C0D12),
    backgroundGlow = Color(0xFF242136),
    card = Color(0xFF17181F),
    cardSecondary = Color(0xFF1F212B),
    glassStart = Color(0x1CFFFFFF),
    glassEnd = Color(0x0AFFFFFF),
    pillBg = Color(0xFF232530),
    pillText = Color(0xFFFFFFFF),
    sheetBg = Color(0xFF1C1D24),
    cancelBg = Color(0xFF2A2C36),
    navBg = Color(0xF20E0F14),
    textPrimary = Color(0xFFF5F6FA),
    textSecondary = Color(0xFFB5B9CA),
    textMuted = Color(0xFF8E92A4),
    border = Color(0x14FFFFFF),
    accent = Color(0xFF8B6EFF),
    danger = Color(0xFFFF4D67),
    contrastSurface = Color(0xFFFFFFFF),
    onContrastSurface = Color(0xFF121112)
)
