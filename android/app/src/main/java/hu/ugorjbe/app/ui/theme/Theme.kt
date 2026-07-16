package hu.ugorjbe.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val UgorjBeLight = lightColorScheme(
    primary = Color(0xFF6547F5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9E3FF),
    onPrimaryContainer = Color(0xFF251073),
    secondary = Color(0xFFE43F5A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9DF),
    onSecondaryContainer = Color(0xFF641324),
    tertiary = Color(0xFF007E75),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF9FF2E8),
    onTertiaryContainer = Color(0xFF00201D),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1A22),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1A22),
    surfaceVariant = Color(0xFFE7E2EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F4FC),
    surfaceContainer = Color(0xFFF2EEF6),
    surfaceContainerHigh = Color(0xFFECE8F0),
    surfaceContainerHighest = Color(0xFFE6E2EA),
    outline = Color(0xFF7A7580),
    outlineVariant = Color(0xFFCBC5D0),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0x99000000),
)

private val UgorjBeDark = darkColorScheme(
    primary = Color(0xFFC9BFFF),
    onPrimary = Color(0xFF34208F),
    primaryContainer = Color(0xFF4C35C0),
    onPrimaryContainer = Color(0xFFE9E3FF),
    secondary = Color(0xFFFFB2BE),
    onSecondary = Color(0xFF8B2138),
    secondaryContainer = Color(0xFFAF3049),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = Color(0xFF82D5CC),
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFF9FF2E8),
    background = Color(0xFF121117),
    onBackground = Color(0xFFE7E0EA),
    surface = Color(0xFF121117),
    onSurface = Color(0xFFE7E0EA),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCBC4D0),
    surfaceContainerLowest = Color(0xFF0D0C11),
    surfaceContainerLow = Color(0xFF1A181F),
    surfaceContainer = Color(0xFF201E25),
    surfaceContainerHigh = Color(0xFF2A282F),
    surfaceContainerHighest = Color(0xFF35323A),
    outline = Color(0xFF958F9A),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    scrim = Color(0xCC000000),
)

private val UgorjBeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1.2).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp),
)

private val UgorjBeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun UgorjBeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) UgorjBeDark else UgorjBeLight,
        typography = UgorjBeTypography,
        shapes = UgorjBeShapes,
        content = content,
    )
}
