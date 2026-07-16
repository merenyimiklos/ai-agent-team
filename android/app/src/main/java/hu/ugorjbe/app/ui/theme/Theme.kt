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
    primary = UgorjBeBrand.Coral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE3DD),
    onPrimaryContainer = Color(0xFF5D1E13),
    secondary = UgorjBeBrand.Forest,
    onSecondary = Color.White,
    secondaryContainer = UgorjBeBrand.Sage,
    onSecondaryContainer = UgorjBeBrand.ForestDeep,
    tertiary = Color(0xFF805A08),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBE6B0),
    onTertiaryContainer = Color(0xFF342400),
    background = UgorjBeBrand.Cream,
    onBackground = UgorjBeBrand.Ink,
    surface = UgorjBeBrand.Paper,
    onSurface = UgorjBeBrand.Ink,
    surfaceVariant = Color(0xFFE4EAE5),
    onSurfaceVariant = Color(0xFF586660),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF8F0),
    surfaceContainer = Color(0xFFF4F0E7),
    surfaceContainerHigh = Color(0xFFECE8DE),
    surfaceContainerHighest = Color(0xFFE4E0D6),
    outline = Color(0xFF718079),
    outlineVariant = Color(0xFFD1D9D3),
    error = Color(0xFFA33D35),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0x990C2B24),
)

private val UgorjBeDark = darkColorScheme(
    primary = Color(0xFFFFB4A7),
    onPrimary = Color(0xFF6F2518),
    primaryContainer = Color(0xFF8D392A),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFA8D5C2),
    onSecondary = Color(0xFF08382E),
    secondaryContainer = Color(0xFF245E4E),
    onSecondaryContainer = Color(0xFFC4EDDD),
    tertiary = Color(0xFFF1C56C),
    onTertiary = Color(0xFF432E00),
    tertiaryContainer = Color(0xFF5F4500),
    onTertiaryContainer = Color(0xFFFFDEA1),
    background = Color(0xFF101714),
    onBackground = Color(0xFFE6EFEA),
    surface = Color(0xFF101714),
    onSurface = Color(0xFFE6EFEA),
    surfaceVariant = Color(0xFF3F4A45),
    onSurfaceVariant = Color(0xFFC2CCC6),
    surfaceContainerLowest = Color(0xFF0B100E),
    surfaceContainerLow = Color(0xFF171F1B),
    surfaceContainer = Color(0xFF1D2622),
    surfaceContainerHigh = Color(0xFF28312D),
    surfaceContainerHighest = Color(0xFF333C38),
    outline = Color(0xFF8C9992),
    outlineVariant = Color(0xFF3E4944),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xD9000000),
)

private val UgorjBeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 50.sp,
        lineHeight = 54.sp,
        letterSpacing = (-1.1).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 37.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
    ),
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
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
