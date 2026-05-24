package com.example.password_vault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = CoralAccent,
    onPrimary = White,
    primaryContainer = CoralDark,
    onPrimaryContainer = White,
    secondary = SlatePrimary,
    onSecondary = White,
    background = DarkSurface,
    onBackground = White,
    surface = DarkCard,
    onSurface = White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = NeutralBg,
    outline = SlatePrimary,
    error = CoralAccent,
    onError = White
)

@Composable
fun PassVaultTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

// Legacy alias used before we created the clean name
@Composable
fun Password_vaultTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = PassVaultTheme(darkTheme = darkTheme, content = content)
