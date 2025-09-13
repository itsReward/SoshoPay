package com.soshopay.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimaryBackground,
        secondary = DarkSecondaryBackground,
        tertiary = DarkTertiaryBackground,
        // text_field_background
        onTertiary = DarkTextFieldBackground,
        surfaceBright = DarkIcons,
        // text headings color
        surface = DarkTextHeadings,
        // form labels color
        onSurface = DarkTextLabels,
        // white color
        surfaceTint = Color.White,
        // Color(0xFF122130)
    )

private val LightColorScheme =
    lightColorScheme(
        // yellow_background
        primary = LightPrimaryBackground,
        secondary = LightSecondaryBackground,
        tertiary = LightTertiaryBackground,
        surfaceBright = LightIcons,
        // text_field_background
        onTertiary = LightTextFieldBackground,
        // text headings color
        surface = LightTextHeadings,
        // form labels color
        onSurface = LightTextLabels,
        // white color
        surfaceTint = Color.White,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Composable
fun SoshoPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                // if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
