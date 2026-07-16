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
    val large = 24.dp
    val hero = 32.dp
    val pill = 100.dp
}

object UgorjBeMotion {
    const val Instant = 90
    const val Quick = 160
    const val Standard = 260
    const val Expressive = 420
    val EnterEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val ExitEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    fun <T> tactileSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun <T> settleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}

object UgorjBeBrand {
    val ElectricViolet = Color(0xFF6547F5)
    val DeepViolet = Color(0xFF3820A8)
    val Coral = Color(0xFFFF5D73)
    val Apricot = Color(0xFFFFB86B)
    val Aqua = Color(0xFF2BC4B6)
    val Ink = Color(0xFF17151F)
    val Paper = Color(0xFFFFFBFF)

    val HeroGradient = Brush.linearGradient(
        listOf(ElectricViolet, Color(0xFF8B5CF6), Coral),
    )
    val WarmGradient = Brush.linearGradient(
        listOf(Coral, Apricot),
    )
    val CoolGradient = Brush.linearGradient(
        listOf(DeepViolet, ElectricViolet, Aqua),
    )
}
