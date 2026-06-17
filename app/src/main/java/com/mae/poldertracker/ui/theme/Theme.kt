package com.mae.poldertracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Turquoise40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = TurquoiseContainer,
    onPrimaryContainer = Neutral20,
    secondary = Aqua40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Neutral90,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = Neutral20,
    onSurface = Neutral20,
)

private val DarkColorScheme = darkColorScheme(
    primary = Turquoise80,
    onPrimary = Neutral20,
    primaryContainer = Turquoise40,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = Aqua80,
    onSecondary = Neutral20,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun PolderTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
