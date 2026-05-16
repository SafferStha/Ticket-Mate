package com.example.individual_project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TicketMateColorScheme = lightColorScheme(
    primary          = TmBlue,
    onPrimary        = TmSurface,
    primaryContainer = TmDarkBlue,
    secondary        = TmDarkBlue,
    onSecondary      = TmSurface,
    background       = TmBackground,
    onBackground     = TmTextPrimary,
    surface          = TmSurface,
    onSurface        = TmTextPrimary,
    error            = TmError,
    onError          = TmSurface,
)

@Composable
fun IndividualProjectTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TicketMateColorScheme,
        typography  = Typography,
        content     = content
    )
}
