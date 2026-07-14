package hu.ugorjbe.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmLight = lightColorScheme(
    primary = Color(0xFF8C3F2D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD0),
    onPrimaryContainer = Color(0xFF3A0B02),
    secondary = Color(0xFF486A58),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC9EED7),
    onSecondaryContainer = Color(0xFF062116),
    tertiary = Color(0xFF765A13),
    tertiaryContainer = Color(0xFFFFE08A),
    background = Color(0xFFFFF8F3),
    surface = Color(0xFFFFF8F3),
    surfaceVariant = Color(0xFFF5DED6),
    outline = Color(0xFF8A716A),
    error = Color(0xFFBA1A1A),
)

private val WarmDark = darkColorScheme(
    primary = Color(0xFFFFB4A2),
    onPrimary = Color(0xFF561F12),
    primaryContainer = Color(0xFF703526),
    secondary = Color(0xFFADD2BC),
    onSecondary = Color(0xFF18382A),
    secondaryContainer = Color(0xFF304F40),
    tertiary = Color(0xFFE7C363),
    background = Color(0xFF1B110E),
    surface = Color(0xFF1B110E),
)

@Composable
fun UgorjBeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) WarmDark else WarmLight,
        content = content,
    )
}
