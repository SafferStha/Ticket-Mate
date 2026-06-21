package com.example.individual_project.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary ──────────────────────────────────────────────────────────────────
val TmBlue        = Color(0xFF026CDF)
val TmBlueDark    = Color(0xFF0257B8)  // pressed / ripple state
val TmBlueLight   = Color(0xFF3D8FE8)  // on dark surfaces

// ─── Secondary / Navy ─────────────────────────────────────────────────────────
val TmNavy        = Color(0xFF0A0F1F)  // Deep Navy – dark bg
val TmNavyMid     = Color(0xFF111827)  // card bg on dark
val TmNavyLight   = Color(0xFF1E2A3A)  // elevated surface on dark

// ─── Accent ───────────────────────────────────────────────────────────────────
val TmPurple      = Color(0xFF7C3AED)
val TmPurpleLight = Color(0xFF9D6FF0)

// ─── Neutral ──────────────────────────────────────────────────────────────────
val TmWhite       = Color(0xFFFFFFFF)
val TmGray100     = Color(0xFFF8F9FA)  // light bg
val TmGray200     = Color(0xFFE5E7EB)  // divider (light)
val TmGray400     = Color(0xFFB3B8C4)  // soft gray / hint text
val TmGray600     = Color(0xFF6B7280)  // secondary text
val TmGray800     = Color(0xFF1F2937)  // dark text on light

// ─── Dark surface hierarchy ───────────────────────────────────────────────────
val TmDarkBg        = Color(0xFF080D1A)  // page background
val TmDarkSurface   = Color(0xFF0F1628)  // card, sheet
val TmDarkElevated  = Color(0xFF162038)  // elevated card, dialog
val TmDarkOutline   = Color(0xFF2A3550)  // borders on dark

// ─── Feedback ─────────────────────────────────────────────────────────────────
val TmSuccess     = Color(0xFF10B981)
val TmSuccessBg   = Color(0xFF064E3B)
val TmError       = Color(0xFFEF4444)
val TmErrorBg     = Color(0xFF7F1D1D)
val TmWarning     = Color(0xFFF59E0B)

// ─── Accent helpers ───────────────────────────────────────────────────────────
val TmGold        = Color(0xFFFFD60A)
val TmOrange      = Color(0xFFFF6B35)

// ─── Legacy aliases (used in existing screens – kept for compatibility) ────────
val TmDarkBlue    = Color(0xFF003A8C)
val TmLightBlue   = Color(0xFF7AB8F5)
val TmNavyBlue    = TmNavy
val TmBackground  = TmGray100
val TmSurface     = TmWhite
val TmTextPrimary   = TmGray800
val TmTextSecondary = TmGray600
val TmTextHint      = TmGray400
val TmDivider       = TmGray200
