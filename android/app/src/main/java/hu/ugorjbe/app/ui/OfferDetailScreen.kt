package hu.ugorjbe.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.ui.viewmodel.OfferDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    viewModel: OfferDetailViewModel,
    onBack: () -> Unit,
    onProvider: (String) -> Unit,
    onBookings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var reservationVisible by remember { mutableStateOf(false) }
    state.booking?.let { booking ->
        BookingSuccessScreen(
            booking = booking,
            onBookings = {
                viewModel.consumeBooking()
                onBookings()
            },
            onBack = {
                viewModel.consumeBooking()
                onBack()
            },
        )
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite, enabled = state.offer != null) {
                        Icon(
                            if (state.favorite) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            stringResource(if (state.favorite) R.string.favorite_remove else R.string.favorite_add),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize().padding(padding))
            state.error != null -> ErrorPane(state.error!!, { viewModel.load() }, Modifier.fillMaxSize().padding(padding))
            state.offer != null -> OfferDetailContent(
                offer = state.offer!!,
                quantity = state.quantity,
                reserving = state.reserving,
                onQuantity = viewModel::setQuantity,
                onReserve = { reservationVisible = true },
                onProvider = { onProvider(state.offer!!.provider.id) },
                modifier = Modifier.padding(padding),
            )
        }
    }
    state.message?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(errorText(error)) },
            confirmButton = { TextButton(onClick = viewModel::dismissMessage) { Text(stringResource(R.string.close)) } },
        )
    }
    if (reservationVisible && state.offer != null) {
        val offer = state.offer!!
        val total = Money(offer.discountedUnitPrice.amount.multiply(state.quantity.toBigDecimal()), offer.discountedUnitPrice.currency)
        AlertDialog(
            onDismissRequest = { reservationVisible = false },
            title = { Text(stringResource(R.string.reservation_review)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(offer.title, style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.quantity)} ${state.quantity}")
                    Text("${stringResource(R.string.total_price)} ${formatMoney(total)}", fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.pay_on_arrival))
                    Text("${stringResource(R.string.cancellation_deadline)} ${formatDateTime(offer.cancelUntilUtc)}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reservationVisible = false
                        viewModel.reserve()
                    },
                ) { Text(stringResource(R.string.confirm_reservation)) }
            },
            dismissButton = { TextButton(onClick = { reservationVisible = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun OfferDetailContent(
    offer: OfferDetail,
    quantity: Int,
    reserving: Boolean,
    onQuantity: (Int) -> Unit,
    onReserve: () -> Unit,
    onProvider: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(18.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) { Text(stringResource(R.string.discount, offer.discountPercent), Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontWeight = FontWeight.Bold) }
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(categoryLabel(offer.category)), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(offer.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${formatDateTime(offer.startsAtUtc)} – ${formatTime(offer.endsAtUtc)}", style = MaterialTheme.typography.titleMedium)
            Text(offer.description, style = MaterialTheme.typography.bodyLarge)
            Card(onClick = onProvider, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.provider), style = MaterialTheme.typography.labelMedium)
                        Text(offer.provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${offer.address.postalCode} ${offer.address.city}, ${offer.address.street}")
                    }
                    Icon(Icons.Outlined.ChevronRight, stringResource(R.string.view_provider))
                }
            }
            InfoRow(Icons.Outlined.Groups, stringResource(R.string.age_range, offer.minChildAge, offer.maxChildAge))
            if (offer.accompanimentRequired) InfoRow(Icons.Outlined.Groups, stringResource(R.string.accompaniment_required))
            offer.accessibilityInfo?.let { InfoRow(Icons.Outlined.Accessible, "${stringResource(R.string.accessibility)}: $it") }
            InfoRow(Icons.Outlined.Payments, stringResource(R.string.pay_on_arrival))
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(formatMoney(offer.originalUnitPrice), textDecoration = TextDecoration.LineThrough)
                    Text(formatMoney(offer.discountedUnitPrice), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Text(stringResource(R.string.places_left, offer.availablePlaces), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            if (offer.isBookable && offer.availablePlaces > 0) {
                Text(stringResource(R.string.quantity), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { onQuantity(quantity - 1) }, enabled = quantity > 1) { Text("−") }
                    Text(quantity.toString(), style = MaterialTheme.typography.headlineSmall)
                    OutlinedButton(onClick = { onQuantity(quantity + 1) }, enabled = quantity < offer.availablePlaces.coerceAtMost(10)) { Text("+") }
                }
                Button(onClick = onReserve, modifier = Modifier.fillMaxWidth(), enabled = !reserving) {
                    if (reserving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.reserve))
                }
            } else {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.booking_closed), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        Text(text, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingSuccessScreen(booking: Booking, onBookings: () -> Unit, onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.booking_success)) }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.Outlined.QrCode2, null, Modifier.padding(28.dp).size(92.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.booking_success), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(booking.offer.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.booking_code), style = MaterialTheme.typography.labelLarge)
            Text(booking.bookingCode, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.show_at_arrival), textAlign = TextAlign.Center)
            TextButton(onClick = { clipboard.setText(AnnotatedString(booking.bookingCode)) }) {
                Text(stringResource(R.string.copy_booking_code))
            }
            Spacer(Modifier.height(20.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.qr_payload), style = MaterialTheme.typography.labelMedium)
                    Text(booking.qrPayload, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBookings, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.bookings)) }
            TextButton(onClick = onBack) { Text(stringResource(R.string.back_to_discover)) }
        }
    }
}
