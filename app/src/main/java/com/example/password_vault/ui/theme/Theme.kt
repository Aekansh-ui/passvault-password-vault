package com.example.password_vault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CoralAccent,
    onPrimary = White,
    primaryContainer = CoralAccent,
    onPrimaryContainer = White,
    secondary = SlatePrimary,
    onSecondary = White,
    background = White,
    onBackground = SlatePrimary,
    surface = White,
    onSurface = SlatePrimary,
    surfaceVariant = NeutralCard,
    onSurfaceVariant = SlatePrimary,
    outline = DividerGrey,
    error = CoralDark,
    onError = White
)

@Composable
fun PassVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
