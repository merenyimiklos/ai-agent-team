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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
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
    val booking = state.booking
    val offer = state.offer
    val error = state.error
    var reservationVisible by remember { mutableStateOf(false) }

    if (booking != null) {
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_to_discover))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite, enabled = offer != null) {
                        Icon(
                            if (state.favorite) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            stringResource(if (state.favorite) R.string.favorite_remove else R.string.favorite_add),
                            tint = if (state.favorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        bottomBar = {
            offer?.let { currentOffer ->
                Phase3BookingBar(
                    offer = currentOffer,
                    quantity = state.quantity,
                    reserving = state.reserving,
                    onReserve = { reservationVisible = true },
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize().padding(padding))
            error != null -> ErrorPane(error, { viewModel.load() }, Modifier.fillMaxSize().padding(padding))
            offer != null -> OfferDetailContent(
                offer = offer,
                quantity = state.quantity,
                onQuantity = viewModel::setQuantity,
                onProvider = { onProvider(offer.provider.id) },
                modifier = Modifier.padding(padding),
            )
        }
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(errorText(message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissMessage) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }

    if (reservationVisible && offer != null) {
        ReservationSheet(
            offer = offer,
            quantity = state.quantity,
            reserving = state.reserving,
            onDismiss = { reservationVisible = false },
            onConfirm = {
                reservationVisible = false
                viewModel.reserve()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationSheet(
    offer: OfferDetail,
    quantity: Int,
    reserving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val total = Money(
        offer.discountedUnitPrice.amount.multiply(quantity.toBigDecimal()),
        offer.discountedUnitPrice.currency,
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = UgorjBeSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
        ) {
            Text(stringResource(R.string.reservation_summary), style = MaterialTheme.typography.headlineSmall)
            Text(offer.title, style = MaterialTheme.typography.titleLarge)
            Surface(
                shape = RoundedCornerShape(UgorjBeRadius.large),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    Modifier.padding(UgorjBeSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
                ) {
                    SummaryRow(stringResource(R.string.quantity), quantity.toString())
                    SummaryRow(stringResource(R.string.price_per_person), formatMoney(offer.discountedUnitPrice))
                    HorizontalDivider()
                    SummaryRow(stringResource(R.string.total_price), formatMoney(total), prominent = true)
                }
            }
            Text(stringResource(R.string.pay_on_arrival), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${stringResource(R.string.cancellation_deadline)} ${formatDateTime(offer.cancelUntilUtc)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.safe_booking_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !reserving,
                shape = RoundedCornerShape(UgorjBeRadius.medium),
            ) {
                Text(stringResource(R.string.confirm_reservation))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(Modifier.height(UgorjBeSpacing.md))
        }
    }
}

@Composable
private fun OfferDetailContent(
    offer: OfferDetail,
    quantity: Int,
    onQuantity: (Int) -> Unit,
    onProvider: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(286.dp)) {
            ExperienceImage(
                imageUrl = offer.imageUrl ?: offer.provider.imageUrl,
                category = offer.category,
                contentDescription = stringResource(R.string.image_description, offer.title),
                modifier = Modifier.fillMaxSize(),
            )
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(UgorjBeSpacing.lg),
                shape = RoundedCornerShape(UgorjBeRadius.pill),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ) {
                Text(
                    stringResource(categoryLabel(offer.category)),
                    Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(UgorjBeSpacing.lg),
                shape = RoundedCornerShape(UgorjBeRadius.pill),
                color = MaterialTheme.colorScheme.secondary,
            ) {
                Text(
                    stringResource(R.string.discount, offer.discountPercent),
                    Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Column(
            Modifier.padding(UgorjBeSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm)) {
                Text(offer.title, style = MaterialTheme.typography.headlineLarge)
                DetailMeta(Icons.Outlined.Schedule, "${formatDateTime(offer.startsAtUtc)} – ${formatTime(offer.endsAtUtc)}")
                DetailMeta(Icons.Outlined.LocationOn, "${offer.address.postalCode} ${offer.address.city}, ${offer.address.street}")
            }

            Text(offer.description, style = MaterialTheme.typography.bodyLarge)

            Surface(
                onClick = onProvider,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UgorjBeRadius.large),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(Modifier.padding(UgorjBeSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    ExperienceImage(
                        imageUrl = offer.provider.imageUrl,
                        category = offer.category,
                        contentDescription = offer.provider.name,
                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                    )
                    Column(Modifier.weight(1f).padding(horizontal = UgorjBeSpacing.md)) {
                        Text(stringResource(R.string.provider), style = MaterialTheme.typography.labelMedium)
                        Text(offer.provider.name, style = MaterialTheme.typography.titleMedium)
                        Text(offer.provider.shortDescription, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Outlined.ChevronRight, stringResource(R.string.view_provider))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm)) {
                InfoPill(
                    Icons.Outlined.Groups,
                    stringResource(R.string.age_range, offer.minChildAge, offer.maxChildAge),
                    Modifier.weight(1f),
                )
                InfoPill(
                    Icons.Outlined.Payments,
                    stringResource(R.string.pay_on_arrival),
                    Modifier.weight(1f),
                )
            }
            if (offer.accompanimentRequired) {
                DetailMeta(Icons.Outlined.Groups, stringResource(R.string.accompaniment_required))
            }
            offer.accessibilityInfo?.let { accessibility ->
                DetailMeta(Icons.Outlined.Accessible, "${stringResource(R.string.accessibility)}: $accessibility")
            }

            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth(),
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
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            if (offer.isBookable && offer.availablePlaces > 0) {
                Text(stringResource(R.string.quantity), style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    OutlinedButton(
                        onClick = { onQuantity(quantity - 1) },
                        enabled = quantity > 1,
                        shape = CircleShape,
                    ) { Text("−") }
                    Text(quantity.toString(), style = MaterialTheme.typography.headlineSmall)
                    OutlinedButton(
                        onClick = { onQuantity(quantity + 1) },
                        enabled = quantity < offer.availablePlaces.coerceAtMost(10),
                        shape = CircleShape,
                    ) { Text("+") }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(UgorjBeRadius.medium),
                ) {
                    Text(
                        stringResource(R.string.booking_closed),
                        Modifier.padding(UgorjBeSpacing.lg),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun Phase3BookingBar(
    offer: OfferDetail,
    quantity: Int,
    reserving: Boolean,
    onReserve: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 14.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(UgorjBeSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val total = Money(
                offer.discountedUnitPrice.amount.multiply(quantity.toBigDecimal()),
                offer.discountedUnitPrice.currency,
            )
            Column(Modifier.weight(0.7f)) {
                Text(stringResource(R.string.total_price), style = MaterialTheme.typography.labelMedium)
                Text(
                    formatMoney(total),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(
                onClick = onReserve,
                modifier = Modifier.weight(1.3f).height(54.dp),
                enabled = offer.isBookable && offer.availablePlaces > 0 && !reserving,
                shape = RoundedCornerShape(UgorjBeRadius.medium),
            ) {
                if (reserving) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.reserve))
                }
            }
        }
    }
}

@Composable
private fun DetailMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = RoundedCornerShape(UgorjBeRadius.medium),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, prominent: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = if (prominent) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun BookingSuccessScreen(booking: Booking, onBookings: () -> Unit, onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(booking.id) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(UgorjBeSpacing.xxl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Phase3Lottie(
                resource = R.raw.booking_success,
                modifier = Modifier.size(190.dp),
                loop = false,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(
                        Icons.Outlined.QrCode2,
                        null,
                        Modifier.padding(32.dp).size(86.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                stringResource(R.string.booking_success),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(UgorjBeSpacing.sm))
            Text(
                stringResource(R.string.booking_success_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(UgorjBeSpacing.xxl))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UgorjBeRadius.hero),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(
                    Modifier.padding(UgorjBeSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                ) {
                    Text(booking.offer.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.booking_code_hint), style = MaterialTheme.typography.labelMedium)
                    Text(
                        booking.bookingCode,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TextButton(onClick = { clipboard.setText(AnnotatedString(booking.bookingCode)) }) {
                        Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.copy_booking_code))
                    }
                    Surface(
                        shape = RoundedCornerShape(UgorjBeRadius.medium),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(stringResource(R.string.qr_payload), style = MaterialTheme.typography.labelMedium)
                            Text(booking.qrPayload, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(UgorjBeSpacing.xxl))
            Button(
                onClick = onBookings,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(UgorjBeRadius.medium),
            ) {
                Text(stringResource(R.string.bookings))
            }
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back_to_discover))
            }
        }
    }
}
