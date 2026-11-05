package ru.urfu.starwars.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SaberYellow = Color(0xFFFFE81F)
private val SpaceBlue = Color(0xFF1B2A49)
private val DeepSpace = Color(0xFF0B0F1A)

private val DarkColors = darkColorScheme(
    primary = SaberYellow,
    onPrimary = DeepSpace,
    secondary = Color(0xFF8AB4F8),
    background = DeepSpace,
    surface = Color(0xFF121826),
)

private val LightColors = lightColorScheme(
    primary = SpaceBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF6B5B00),
)

@Composable
fun StarWarsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
