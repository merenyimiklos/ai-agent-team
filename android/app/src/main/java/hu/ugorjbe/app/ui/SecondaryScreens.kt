package hu.ugorjbe.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.ui.viewmodel.BookingsViewModel
import hu.ugorjbe.app.ui.viewmodel.FavoritesViewModel
import hu.ugorjbe.app.ui.viewmodel.ProviderViewModel

@Composable
fun BookingsScreen(viewModel: BookingsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var cancelTarget by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.bookings), Modifier.padding(20.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        TabRow(selectedTabIndex = if (state.scope == "ACTIVE") 0 else 1) {
            Tab(selected = state.scope == "ACTIVE", onClick = { viewModel.setScope("ACTIVE") }, text = { Text(stringResource(R.string.active)) })
            Tab(selected = state.scope == "PREVIOUS", onClick = { viewModel.setScope("PREVIOUS") }, text = { Text(stringResource(R.string.previous)) })
        }
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize())
            state.error != null -> ErrorPane(state.error!!, { viewModel.refresh() }, Modifier.fillMaxSize())
            state.bookings.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(if (state.scope == "ACTIVE") R.string.no_active_bookings else R.string.no_previous_bookings))
            }
            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.bookings, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        cancelling = state.cancellingId == booking.id,
                        onCancel = { cancelTarget = booking.id },
                    )
                }
            }
        }
    }
    cancelTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text(stringResource(R.string.cancel_confirm_title)) },
            text = { Text(stringResource(R.string.cancel_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    cancelTarget = null
                    viewModel.cancel(id)
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { cancelTarget = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    state.message?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            text = { Text(errorText(error)) },
            confirmButton = { TextButton(onClick = viewModel::dismissMessage) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun BookingCard(booking: Booking, cancelling: Boolean, onCancel: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.offer.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(statusLabel(booking.status)), color = MaterialTheme.colorScheme.primary)
            }
            Text(booking.offer.providerName)
            Text(formatDateTime(booking.offer.startsAtUtc))
            Text("${booking.quantity} × ${formatMoney(booking.unitPrice)} = ${formatMoney(booking.totalPrice)}")
            if (booking.status == "CONFIRMED") {
                Text("${stringResource(R.string.booking_code)}: ${booking.bookingCode}", fontWeight = FontWeight.Bold)
            }
            if (booking.cancellationAllowed) {
                TextButton(onClick = onCancel, enabled = !cancelling) {
                    if (cancelling) CircularProgressIndicator(Modifier.padding(2.dp))
                    else Text(stringResource(R.string.cancel_booking))
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, onOffer: (String) -> Unit, onProvider: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.favorites), Modifier.padding(20.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.favorite_offers)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.favorite_providers)) })
        }
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize())
            state.error != null -> ErrorPane(state.error!!, { viewModel.refresh() }, Modifier.fillMaxSize())
            tab == 0 && state.offers.isEmpty() -> EmptyText(R.string.no_favorite_offers)
            tab == 1 && state.providers.isEmpty() -> EmptyText(R.string.no_favorite_providers)
            tab == 0 -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(state.offers, key = { it.id }) { OfferCard(it, { onOffer(it.id) }) } }
            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(state.providers, key = { it.id }) { ProviderCard(it) { onProvider(it.id) } } }
        }
    }
}

@Composable
private fun EmptyText(text: Int) = Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { Text(stringResource(text)) }

@Composable
private fun ProviderCard(provider: ProviderSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Business, null)
            Column(Modifier.weight(1f)) {
                Text(provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(provider.shortDescription)
                Text("${provider.address.city}, ${provider.address.street}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScreen(viewModel: ProviderViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.provider)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite, enabled = state.provider != null) {
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
            state.error != null -> ErrorPane(state.error!!, { viewModel.refresh() }, Modifier.fillMaxSize().padding(padding))
            state.provider != null -> {
                val provider = state.provider!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item { Text(provider.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                    item { Text(provider.shortDescription, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium) }
                    item { Text(provider.description, style = MaterialTheme.typography.bodyLarge) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.LocationOn, null)
                            Text("${provider.address.postalCode} ${provider.address.city}, ${provider.address.street}")
                        }
                    }
                    provider.accessibilityInfo?.let { item { Text("${stringResource(R.string.accessibility)}: $it") } }
                    item { HorizontalDivider() }
                    item { Text(stringResource(R.string.provider_offers, provider.activeOfferCount), fontWeight = FontWeight.SemiBold) }
                    if (provider.phone != null || provider.email != null || provider.websiteUrl != null) {
                        item { Text(stringResource(R.string.contact), style = MaterialTheme.typography.titleMedium) }
                        provider.phone?.let { item { Text(it) } }
                        provider.email?.let { item { Text(it) } }
                        provider.websiteUrl?.let { item { Text(it) } }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(displayName: String, email: String, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.profile), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.signed_in_as), style = MaterialTheme.typography.labelLarge)
        Text(displayName, style = MaterialTheme.typography.titleLarge)
        Text(email)
        Text(stringResource(R.string.local_backend), style = MaterialTheme.typography.bodySmall)
        Button(onClick = onLogout) {
            Icon(Icons.Outlined.Logout, null)
            Text(stringResource(R.string.logout), Modifier.padding(start = 8.dp))
        }
    }
}
