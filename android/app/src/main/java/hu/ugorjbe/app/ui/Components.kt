package hu.ugorjbe.app.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.ui.theme.UgorjBeMotion
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val BudapestZone: ZoneId = ZoneId.of("Europe/Budapest")
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("hu", "HU"))
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d., HH:mm", Locale("hu", "HU"))

fun formatTime(value: String): String = runCatching {
    TimeFormatter.format(Instant.parse(value).atZone(BudapestZone))
}.getOrDefault(value)

fun formatDateTime(value: String): String = runCatching {
    DateFormatter.format(Instant.parse(value).atZone(BudapestZone))
}.getOrDefault(value)

fun formatMoney(money: Money): String = runCatching {
    NumberFormat.getCurrencyInstance(Locale("hu", "HU")).apply {
        currency = Currency.getInstance(money.currency)
        maximumFractionDigits = if (money.amount.stripTrailingZeros().scale() > 0) 2 else 0
    }.format(money.amount)
}.getOrElse { "${money.amount.toPlainString()} ${money.currency}" }

@StringRes
fun categoryLabel(category: String): Int = when (category) {
    "PLAYHOUSE" -> R.string.category_playhouse
    "WORKSHOP" -> R.string.category_workshop
    "MOVEMENT" -> R.string.category_movement
    "SWIMMING" -> R.string.category_swimming
    "SPORT" -> R.string.category_sport
    "MUSEUM" -> R.string.category_museum
    "PARENT_CHILD" -> R.string.category_parent_child
    else -> R.string.all_categories
}

@StringRes
fun statusLabel(status: String): Int = when (status) {
    "CANCELLED" -> R.string.status_cancelled
    "COMPLETED" -> R.string.status_completed
    else -> R.string.status_confirmed
}

@Composable
fun LoadingPane(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Phase3Lottie(
                resource = R.raw.brand_loading,
                modifier = Modifier.size(112.dp),
                loop = true,
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(UgorjBeSpacing.md))
            Text(stringResource(R.string.loading), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ErrorPane(error: ApiError, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(UgorjBeSpacing.xxl), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(UgorjBeRadius.large),
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Column(
                modifier = Modifier.padding(UgorjBeSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.padding(14.dp).size(34.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Text(
                    errorText(error),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                if (error.kind == ApiError.Kind.CONTRACT && !error.detail.isNullOrBlank()) {
                    Text(
                        error.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (onRetry != null && error.retryable) {
                    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

@Composable
fun errorText(error: ApiError): String = when (error.kind) {
    ApiError.Kind.NETWORK -> stringResource(R.string.error_network)
    ApiError.Kind.CONTRACT -> stringResource(R.string.error_contract)
    ApiError.Kind.SERVER -> stringResource(R.string.error_server)
    ApiError.Kind.INVALID_CREDENTIALS -> stringResource(R.string.error_invalid_credentials)
    ApiError.Kind.EMAIL_EXISTS -> stringResource(R.string.error_email_exists)
    ApiError.Kind.VALIDATION -> stringResource(R.string.error_validation)
    ApiError.Kind.NOT_FOUND -> stringResource(R.string.error_not_found)
    ApiError.Kind.OFFER_NOT_BOOKABLE -> stringResource(R.string.error_not_bookable)
    ApiError.Kind.INSUFFICIENT_CAPACITY -> stringResource(
        R.string.error_insufficient_capacity,
        error.availablePlaces ?: 0,
    )
    ApiError.Kind.CANCELLATION_NOT_ALLOWED -> stringResource(R.string.error_cancellation)
    else -> stringResource(R.string.error_unknown)
}

@Composable
fun OfferCard(offer: OfferSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(UgorjBeRadius.large), ambientColor = Color.Black.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(176.dp)) {
                ExperienceImage(
                    imageUrl = offer.imageUrl ?: offer.provider.imageUrl,
                    category = offer.category,
                    contentDescription = offer.title,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(UgorjBeSpacing.md),
                    shape = RoundedCornerShape(UgorjBeRadius.pill),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                ) {
                    Text(
                        text = stringResource(categoryLabel(offer.category)),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(UgorjBeSpacing.md),
                    shape = RoundedCornerShape(UgorjBeRadius.pill),
                    color = MaterialTheme.colorScheme.secondary,
                ) {
                    Text(
                        text = stringResource(R.string.discount, offer.discountPercent),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(
                Modifier.padding(UgorjBeSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
            ) {
                Text(
                    offer.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    offer.provider.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Metadata(Icons.Outlined.Schedule, formatTime(offer.startsAtUtc))
                    Metadata(Icons.Outlined.LocationOn, offer.address.city)
                    offer.distanceKm?.let { Metadata(null, stringResource(R.string.distance, it.toDouble())) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PriceBlock(offer.originalUnitPrice, offer.discountedUnitPrice)
                    Surface(
                        shape = RoundedCornerShape(UgorjBeRadius.pill),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            stringResource(R.string.places_left, offer.availablePlaces),
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PriceBlock(original: Money, discounted: Money, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (original.amount > discounted.amount) {
            Text(
                formatMoney(original),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        Text(
            formatMoney(discounted),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun Metadata(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            Icon(it, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun OfferCardSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "offer-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(UgorjBeMotion.Expressive),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton-alpha",
    )
    Card(
        modifier = modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(UgorjBeRadius.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(176.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            Column(Modifier.padding(UgorjBeSpacing.lg), verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md)) {
                Box(Modifier.fillMaxWidth(0.72f).height(22.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest))
                Box(Modifier.fillMaxWidth(0.45f).height(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest))
                Box(Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            }
        }
    }
}
