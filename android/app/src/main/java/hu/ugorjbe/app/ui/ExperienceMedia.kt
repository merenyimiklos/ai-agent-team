package hu.ugorjbe.app.ui

import android.provider.Settings
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import hu.ugorjbe.app.ui.theme.UgorjBeBrand

internal data class CategoryArtwork(
    val icon: ImageVector,
    val colors: List<Color>,
)

internal fun categoryArtwork(category: String): CategoryArtwork = when (category) {
    "PLAYHOUSE" -> CategoryArtwork(
        Icons.Outlined.ChildCare,
        listOf(UgorjBeBrand.Coral, UgorjBeBrand.Sun),
    )
    "WORKSHOP" -> CategoryArtwork(
        Icons.Outlined.Palette,
        listOf(UgorjBeBrand.Forest, UgorjBeBrand.Coral),
    )
    "MOVEMENT" -> CategoryArtwork(
        Icons.Outlined.FitnessCenter,
        listOf(UgorjBeBrand.ForestBright, Color(0xFF79B69C)),
    )
    "SWIMMING" -> CategoryArtwork(
        Icons.Outlined.Pool,
        listOf(Color(0xFF176B72), Color(0xFF7BC8C4)),
    )
    "SPORT" -> CategoryArtwork(
        Icons.Outlined.SportsBasketball,
        listOf(UgorjBeBrand.CoralDeep, UgorjBeBrand.Sun),
    )
    "MUSEUM" -> CategoryArtwork(
        Icons.Outlined.Museum,
        listOf(UgorjBeBrand.ForestDeep, Color(0xFF7C6842)),
    )
    "PARENT_CHILD" -> CategoryArtwork(
        Icons.Outlined.AutoAwesome,
        listOf(Color(0xFF8E4A3B), UgorjBeBrand.Coral, UgorjBeBrand.Sun),
    )
    else -> CategoryArtwork(
        Icons.Outlined.AutoAwesome,
        listOf(UgorjBeBrand.Forest, UgorjBeBrand.Coral),
    )
}

@Composable
internal fun ExperienceImage(
    imageUrl: String?,
    category: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var failed by remember(imageUrl) { mutableStateOf(imageUrl.isNullOrBlank()) }
    Box(modifier = modifier) {
        CategoryFallback(category, Modifier.fillMaxSize())
        if (!imageUrl.isNullOrBlank() && !failed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onError = { failed = true },
                onSuccess = { failed = false },
            )
        }
    }
}

@Composable
internal fun CategoryFallback(category: String, modifier: Modifier = Modifier) {
    val artwork = remember(category) { categoryArtwork(category) }
    Box(
        modifier = modifier.background(Brush.linearGradient(artwork.colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = artwork.icon,
            contentDescription = null,
            modifier = Modifier.size(58.dp),
            tint = Color.White.copy(alpha = 0.94f),
        )
    }
}

@Composable
internal fun Phase3Lottie(
    @RawRes resource: Int,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
    fallback: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resource))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        isPlaying = !reducedMotion,
        restartOnPlay = false,
    )

    Box(modifier, contentAlignment = Alignment.Center) {
        fallback()
        composition?.let {
            LottieAnimation(
                composition = it,
                progress = { if (reducedMotion) 1f else progress },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
