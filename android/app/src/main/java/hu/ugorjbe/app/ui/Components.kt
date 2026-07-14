package hu.ugorjbe.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.OfferSummary
import java.math.BigDecimal
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
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.loading))
        }
    }
}

@Composable
fun ErrorPane(error: ApiError, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ErrorOutline, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Text(errorText(error), style = MaterialTheme.typography.bodyLarge)
            if (onRetry != null && error.retryable) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

@Composable
fun errorText(error: ApiError): String = when (error.kind) {
    ApiError.Kind.NETWORK -> stringResource(R.string.error_network)
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
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(92.dp).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 24.dp).size(42.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = stringResource(R.string.discount, offer.discountPercent),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(categoryLabel(offer.category)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(offer.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(offer.provider.name, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(formatTime(offer.startsAtUtc)) })
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.places_left, offer.availablePlaces)) })
                    offer.distanceKm?.let {
                        AssistChip(onClick = {}, label = { Text(stringResource(R.string.distance, it.toDouble())) })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            formatMoney(offer.originalUnitPrice),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                        )
                        Text(formatMoney(offer.discountedUnitPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(18.dp))
                        Text(offer.provider.address.city, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
