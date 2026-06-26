package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TwilightVoidColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Slate950,
    secondary = IndigoGlow,
    onSecondary = Slate950,
    tertiary = EmeraldGlow,
    onTertiary = Slate950,
    background = Slate950,
    surface = Slate900,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = CrimsonAlert,
    onError = TextPrimary,
    outline = GlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our custom premium dark theme "Twilight Void" regardless of system setting,
    // as requested by the premium styling specification.
    val colorScheme = TwilightVoidColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Slate950.toArgb()
            window.navigationBarColor = Slate950.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
