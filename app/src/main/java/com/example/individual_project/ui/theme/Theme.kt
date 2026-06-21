package com.example.individual_project.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ─── Dark scheme (default / "dark-first" design direction) ────────────────────
private val TicketMateDarkColors = darkColorScheme(
    primary              = TmBlue,
    onPrimary            = TmWhite,
    primaryContainer     = TmBlueDark,
    onPrimaryContainer   = TmBlueLight,

    secondary            = TmPurple,
    onSecondary          = TmWhite,
    secondaryContainer   = Color(0xFF3B1F7A),
    onSecondaryContainer = TmPurpleLight,

    tertiary             = TmGold,
    onTertiary           = TmNavy,

    background           = TmDarkBg,
    onBackground         = TmWhite,

    surface              = TmDarkSurface,
    onSurface            = TmWhite,
    surfaceVariant       = TmDarkElevated,
    onSurfaceVariant     = TmGray400,

    outline              = TmDarkOutline,
    outlineVariant       = TmNavyLight,

    error                = TmError,
    onError              = TmWhite,
    errorContainer       = TmErrorBg,
    onErrorContainer     = TmError,
)

// ─── Light scheme (fallback / accessibility) ──────────────────────────────────
private val TicketMateLightColors = lightColorScheme(
    primary              = TmBlue,
    onPrimary            = TmWhite,
    primaryContainer     = TmLightBlue,
    onPrimaryContainer   = TmDarkBlue,

    secondary            = TmPurple,
    onSecondary          = TmWhite,
    secondaryContainer   = Color(0xFFEDE9FE),
    onSecondaryContainer = TmPurple,

    tertiary             = TmGold,
    onTertiary           = TmNavy,

    background           = TmGray100,
    onBackground         = TmGray800,

    surface              = TmWhite,
    onSurface            = TmGray800,
    surfaceVariant       = TmGray200,
    onSurfaceVariant     = TmGray600,

    outline              = TmGray200,
    outlineVariant       = TmGray400,

    error                = TmError,
    onError              = TmWhite,
    errorContainer       = Color(0xFFFFE4E4),
    onErrorContainer     = TmError,
)

@Composable
fun IndividualProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> TicketMateDarkColors
        else      -> TicketMateLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = TmShapes,
        content     = content
    )
}

// Convenience alias so callers can use either name
typealias TicketMateTheme = IndividualProjectTheme
