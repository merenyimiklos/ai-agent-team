package hu.ugorjbe.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
import hu.ugorjbe.app.ui.viewmodel.BookingsViewModel
import hu.ugorjbe.app.ui.viewmodel.FavoritesViewModel
import hu.ugorjbe.app.ui.viewmodel.ProviderViewModel

@Composable
fun BookingsScreen(viewModel: BookingsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error = state.error
    var cancelTarget by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(
            eyebrow = stringResource(R.string.your_next_adventure),
            title = stringResource(R.string.bookings),
        )
        TabRow(
            selectedTabIndex = if (state.scope == "ACTIVE") 0 else 1,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Tab(
                selected = state.scope == "ACTIVE",
                onClick = { viewModel.setScope("ACTIVE") },
                text = { Text(stringResource(R.string.active)) },
            )
            Tab(
                selected = state.scope == "PREVIOUS",
                onClick = { viewModel.setScope("PREVIOUS") },
                text = { Text(stringResource(R.string.previous)) },
            )
        }
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize())
            error != null -> ErrorPane(error, { viewModel.refresh() }, Modifier.fillMaxSize())
            state.bookings.isEmpty() -> Phase3EmptyState(
                title = stringResource(R.string.empty_bookings_title),
                body = stringResource(R.string.empty_bookings_body),
                modifier = Modifier.fillMaxSize(),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = UgorjBeSpacing.xl,
                    end = UgorjBeSpacing.xl,
                    top = UgorjBeSpacing.lg,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
            ) {
                items(state.bookings, key = { it.id }) { booking ->
                    Phase3BookingCard(
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
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            text = { Text(errorText(message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissMessage) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}

@Composable
private fun Phase3BookingCard(booking: Booking, cancelling: Boolean, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            ExperienceImage(
                imageUrl = booking.offer.imageUrl,
                category = booking.offer.category,
                contentDescription = booking.offer.title,
                modifier = Modifier.fillMaxWidth().height(138.dp),
            )
            Column(
                Modifier.padding(UgorjBeSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        booking.offer.title,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.size(8.dp))
                    Surface(
                        shape = RoundedCornerShape(UgorjBeRadius.pill),
                        color = if (booking.status == "CONFIRMED") {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    ) {
                        Text(
                            stringResource(statusLabel(booking.status)),
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Text(booking.offer.providerName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatDateTime(booking.offer.startsAtUtc),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${booking.quantity} × ${formatMoney(booking.unitPrice)}")
                    Text(formatMoney(booking.totalPrice), fontWeight = FontWeight.ExtraBold)
                }
                if (booking.status == "CONFIRMED") {
                    Surface(
                        shape = RoundedCornerShape(UgorjBeRadius.medium),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(R.string.booking_code_hint), style = MaterialTheme.typography.labelMedium)
                            Text(
                                booking.bookingCode,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                if (booking.cancellationAllowed) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !cancelling,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (cancelling) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.cancel_booking))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onOffer: (String) -> Unit,
    onProvider: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error = state.error
    var tab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(
            eyebrow = stringResource(R.string.saved_for_later),
            title = stringResource(R.string.favorites),
        )
        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text(stringResource(R.string.favorite_offers)) },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text(stringResource(R.string.favorite_providers)) },
            )
        }
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize())
            error != null -> ErrorPane(error, { viewModel.refresh() }, Modifier.fillMaxSize())
            tab == 0 && state.offers.isEmpty() -> Phase3EmptyState(
                stringResource(R.string.empty_favorites_title),
                stringResource(R.string.empty_favorites_body),
                Modifier.fillMaxSize(),
            )
            tab == 1 && state.providers.isEmpty() -> Phase3EmptyState(
                stringResource(R.string.empty_favorites_title),
                stringResource(R.string.no_favorite_providers),
                Modifier.fillMaxSize(),
            )
            tab == 0 -> LazyColumn(
                contentPadding = PaddingValues(
                    start = UgorjBeSpacing.xl,
                    end = UgorjBeSpacing.xl,
                    top = UgorjBeSpacing.lg,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
            ) {
                items(state.offers, key = { it.id }) { offer ->
                    OfferCard(offer, { onOffer(offer.id) })
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = UgorjBeSpacing.xl,
                    end = UgorjBeSpacing.xl,
                    top = UgorjBeSpacing.lg,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
            ) {
                items(state.providers, key = { it.id }) { provider ->
                    Phase3ProviderCard(provider) { onProvider(provider.id) }
                }
            }
        }
    }
}

@Composable
private fun Phase3EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        ) {
            Phase3Lottie(R.raw.empty_discovery, Modifier.size(156.dp), loop = true) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    null,
                    Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Phase3ProviderCard(provider: ProviderSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            Modifier.padding(UgorjBeSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExperienceImage(
                imageUrl = provider.imageUrl,
                category = "PARENT_CHILD",
                contentDescription = provider.name,
                modifier = Modifier.size(82.dp).clip(RoundedCornerShape(UgorjBeRadius.medium)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(provider.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    provider.shortDescription,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${provider.address.city}, ${provider.address.street}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScreen(viewModel: ProviderViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val provider = state.provider
    val error = state.error

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, stringResource(R.string.close))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite, enabled = provider != null) {
                        Icon(
                            if (state.favorite) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            stringResource(if (state.favorite) R.string.favorite_remove else R.string.favorite_add),
                            tint = if (state.favorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize().padding(padding))
            error != null -> ErrorPane(error, { viewModel.refresh() }, Modifier.fillMaxSize().padding(padding))
            provider != null -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
            ) {
                item {
                    ExperienceImage(
                        imageUrl = provider.imageUrl,
                        category = "PARENT_CHILD",
                        contentDescription = provider.name,
                        modifier = Modifier.fillMaxWidth().height(246.dp),
                    )
                }
                item {
                    Column(
                        Modifier.padding(horizontal = UgorjBeSpacing.xl),
                        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                    ) {
                        Text(provider.name, style = MaterialTheme.typography.headlineLarge)
                        Text(
                            provider.shortDescription,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(provider.description, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.padding(horizontal = UgorjBeSpacing.xl).fillMaxWidth(),
                        shape = RoundedCornerShape(UgorjBeRadius.large),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            Modifier.padding(UgorjBeSpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
                        ) {
                            ContactRow(
                                Icons.Outlined.LocationOn,
                                "${provider.address.postalCode} ${provider.address.city}, ${provider.address.street}",
                            )
                            provider.phone?.let { ContactRow(Icons.Outlined.Phone, it) }
                            provider.email?.let { ContactRow(Icons.Outlined.Email, it) }
                            provider.websiteUrl?.let { ContactRow(Icons.Outlined.Public, it) }
                        }
                    }
                }
                provider.accessibilityInfo?.let { accessibility ->
                    item {
                        Text(
                            "${stringResource(R.string.accessibility)}: $accessibility",
                            Modifier.padding(horizontal = UgorjBeSpacing.xl),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.provider_offers, provider.activeOfferCount),
                        Modifier.padding(horizontal = UgorjBeSpacing.xl),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(value, Modifier.weight(1f))
    }
}

@Composable
fun ProfileScreen(displayName: String, email: String, onLogout: () -> Unit) {
    val initials = displayName
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = UgorjBeSpacing.xl,
            end = UgorjBeSpacing.xl,
            bottom = 112.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
    ) {
        item {
            Column(
                Modifier.statusBarsPadding().padding(top = UgorjBeSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
            ) {
                Text(stringResource(R.string.profile_greeting, displayName), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.profile_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(UgorjBeRadius.hero),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(UgorjBeSpacing.xl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(Modifier.size(72.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                initials,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    Column(Modifier.weight(1f).padding(horizontal = UgorjBeSpacing.lg)) {
                        Text(displayName, style = MaterialTheme.typography.titleLarge)
                        Text(email, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item { Text(stringResource(R.string.profile_experience), style = MaterialTheme.typography.titleMedium) }
        item {
            SettingsCard {
                SettingsRow(
                    Icons.Outlined.DarkMode,
                    stringResource(R.string.appearance),
                    stringResource(R.string.system_theme),
                )
                HorizontalDivider()
                SettingsRow(Icons.Outlined.Language, stringResource(R.string.profile_locale), "hu-HU")
                HorizontalDivider()
                SettingsRow(
                    Icons.Outlined.LocationOn,
                    stringResource(R.string.use_my_location),
                    stringResource(R.string.profile_location_note),
                )
            }
        }
        if (BuildConfig.DEBUG) {
            item {
                Surface(
                    shape = RoundedCornerShape(UgorjBeRadius.medium),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Text(
                        stringResource(R.string.local_backend),
                        Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
        item {
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.Logout, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = UgorjBeSpacing.lg)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = UgorjBeSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(icon, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f).padding(horizontal = UgorjBeSpacing.md)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScreenHeader(eyebrow: String, title: String) {
    Column(
        Modifier.statusBarsPadding().padding(
            horizontal = UgorjBeSpacing.xl,
            vertical = UgorjBeSpacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineLarge)
    }
}
