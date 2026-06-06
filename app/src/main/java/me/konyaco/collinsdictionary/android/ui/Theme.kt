package me.konyaco.collinsdictionary.android.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CollinsRed = Color(0xFFD32F2F)
private val CollinsRedDark = Color(0xFFE57373)

private val LightColors = lightColorScheme(
    primary = CollinsRed,
    onPrimary = Color.White,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F0F0),
)

private val DarkColors = darkColorScheme(
    primary = CollinsRedDark,
    onPrimary = Color.White,
    background = Color(0xFF161616),
    surface = Color(0xFF202020),
    surfaceVariant = Color(0xFF303030),
)

@Composable
fun CollinsDictionaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
