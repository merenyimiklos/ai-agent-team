package hu.ugorjbe.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object UgorjBeSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
}

object UgorjBeRadius {
    val small = 10.dp
    val medium = 16.dp
    val large = 22.dp
    val hero = 28.dp
    val pill = 100.dp
}

object UgorjBeMotion {
    const val Instant = 90
    const val Quick = 150
    const val Standard = 240
    const val Expressive = 380
    val EnterEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val ExitEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    fun <T> tactileSpring() = spring<T>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMedium,
    )

    fun <T> settleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}

object UgorjBeBrand {
    val Forest = Color(0xFF173F35)
    val ForestBright = Color(0xFF2B6958)
    val ForestDeep = Color(0xFF0C2B24)
    val Coral = Color(0xFFEB705B)
    val CoralDeep = Color(0xFFC95745)
    val Sun = Color(0xFFF4BD58)
    val Sage = Color(0xFFDCE9D9)
    val Cream = Color(0xFFF6F2E9)
    val Paper = Color(0xFFFFFDF7)
    val Mist = Color(0xFFE8EFEA)
    val Ink = Color(0xFF173F35)
    val ElectricViolet = Color(0xFF6547F5)

    val HeroGradient = Brush.linearGradient(
        listOf(ForestDeep, Forest, ForestBright),
    )
    val WarmGradient = Brush.linearGradient(
        listOf(Coral, Sun),
    )
    val CoolGradient = Brush.linearGradient(
        listOf(Forest, ForestBright, Color(0xFF79B69C)),
    )
    val CreamGradient = Brush.linearGradient(
        listOf(Paper, Cream, Sage.copy(alpha = 0.82f)),
    )
}
