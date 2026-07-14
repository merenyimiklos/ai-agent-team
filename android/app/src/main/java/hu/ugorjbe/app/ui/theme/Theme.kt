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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CityPicnicLight = lightColorScheme(
    primary = Color(0xFF76264B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD8E7),
    onPrimaryContainer = Color(0xFF351022),
    secondary = Color(0xFF176A63),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA6F2E9),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFF805600),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEA0),
    onTertiaryContainer = Color(0xFF281900),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF21191C),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF21191C),
    surfaceContainerLow = Color(0xFFFFF0F3),
    surfaceContainer = Color(0xFFF9E9EE),
    surfaceContainerHigh = Color(0xFFF2E1E7),
    onSurfaceVariant = Color(0xFF51434A),
    outline = Color(0xFF84747B),
    outlineVariant = Color(0xFFD7C2CA),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0x66000000),
)

private val CityPicnicDark = darkColorScheme(
    primary = Color(0xFFFFAFD0),
    onPrimary = Color(0xFF45102B),
    primaryContainer = Color(0xFF5D173A),
    onPrimaryContainer = Color(0xFFFFD8E7),
    secondary = Color(0xFF8BD7CF),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF004F49),
    onSecondaryContainer = Color(0xFFA6F2E9),
    tertiary = Color(0xFFF7C765),
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF604000),
    onTertiaryContainer = Color(0xFFFFDEA0),
    background = Color(0xFF181114),
    onBackground = Color(0xFFEEDFE3),
    surface = Color(0xFF181114),
    onSurface = Color(0xFFEEDFE3),
    surfaceContainerLow = Color(0xFF22191D),
    surfaceContainer = Color(0xFF291F23),
    surfaceContainerHigh = Color(0xFF342A2E),
    onSurfaceVariant = Color(0xFFD5C2C9),
    outline = Color(0xFF9E8D94),
    outlineVariant = Color(0xFF51434A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0x99000000),
)

private val UgorjBeTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)

private val UgorjBeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun UgorjBeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CityPicnicDark else CityPicnicLight,
        typography = UgorjBeTypography,
        shapes = UgorjBeShapes,
        content = content,
    )
}
