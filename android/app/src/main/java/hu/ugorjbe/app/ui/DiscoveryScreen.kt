package hu.ugorjbe.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.MapViewport
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.ui.viewmodel.DiscoveryViewModel
import hu.ugorjbe.app.ui.viewmodel.ExplorePresentation
import hu.ugorjbe.app.ui.viewmodel.LocationPermissionState
import java.math.BigDecimal
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private val categories = listOf<String?>(
    null, "PLAYHOUSE", "WORKSHOP", "MOVEMENT", "SWIMMING", "SPORT", "MUSEUM", "PARENT_CHILD",
)
private val Budapest = LatLng(47.4979, 19.0402)

private data class OfferClusterItem(val offer: OfferSummary) : ClusterItem {
    override fun getPosition() = LatLng(offer.address.latitude.toDouble(), offer.address.longitude.toDouble())
    override fun getTitle() = offer.title
    override fun getSnippet() = offer.provider.name
    override fun getZIndex() = 0f
}

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel, onOffer: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val permanentlyDenied = !granted && activity?.let {
            !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION)
        } == true
        viewModel.onLocationPermissionResult(granted, permanentlyDenied)
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        when (state.presentation) {
            ExplorePresentation.MAP -> MapExplore(
                state = state,
                expanded = expanded,
                onQuery = viewModel::setQuery,
                onSubmitQuery = viewModel::submitQuery,
                onFilters = viewModel::openFilters,
                onList = { viewModel.setPresentation(ExplorePresentation.LIST) },
                onCameraIdle = viewModel::onCameraIdle,
                onProgrammaticCameraMove = viewModel::onProgrammaticCameraMove,
                onSearchArea = viewModel::searchThisArea,
                onSelect = viewModel::selectOffer,
                onOffer = onOffer,
                onRetry = viewModel::retry,
                onLocate = viewModel::requestLocation,
                onOpenListFallback = { viewModel.setPresentation(ExplorePresentation.LIST) },
            )
            ExplorePresentation.LIST -> ListExplore(
                state = state,
                onQuery = viewModel::setQuery,
                onSubmitQuery = viewModel::submitQuery,
                onFilters = viewModel::openFilters,
                onMap = { viewModel.setPresentation(ExplorePresentation.MAP) },
                onOffer = onOffer,
                onRetry = viewModel::retry,
            )
        }
    }

    state.filterDraft?.let { draft ->
        FilterDialog(
            current = draft,
            onDismiss = viewModel::dismissFilters,
            onApply = viewModel::applyFilter,
        )
    }

    if (state.locationPermission == LocationPermissionState.RATIONALE) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLocationRationale,
            icon = { Icon(Icons.Outlined.MyLocation, null) },
            title = { Text(stringResource(R.string.location_rationale_title)) },
            text = { Text(stringResource(R.string.location_rationale_body)) },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }) {
                    Text(stringResource(R.string.continue_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLocationRationale) {
                    Text(stringResource(R.string.not_now))
                }
            },
        )
    }

    if (state.locationPermission == LocationPermissionState.PERMANENTLY_DENIED) {
        AlertDialog(
            onDismissRequest = { viewModel.onLocationPermissionResult(false) },
            title = { Text(stringResource(R.string.location_denied_title)) },
            text = { Text(stringResource(R.string.location_denied_body)) },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        },
                    )
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onLocationPermissionResult(false) }) {
                    Text(stringResource(R.string.continue_without_location))
                }
            },
        )
    }
}

@Composable
private fun MapExplore(
    state: hu.ugorjbe.app.ui.viewmodel.ExploreUiState,
    expanded: Boolean,
    onQuery: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onFilters: () -> Unit,
    onList: () -> Unit,
    onCameraIdle: (MapViewport) -> Unit,
    onProgrammaticCameraMove: () -> Unit,
    onSearchArea: () -> Unit,
    onSelect: (String?) -> Unit,
    onOffer: (String) -> Unit,
    onRetry: () -> Unit,
    onLocate: () -> Unit,
    onOpenListFallback: () -> Unit,
) {
    val context = LocalContext.current
    val hasRealMapKey = BuildConfig.MAPS_API_KEY.isNotBlank() && BuildConfig.MAPS_API_KEY != "DEFAULT_API_KEY"
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Budapest, 12f)
    }
    val mapScope = rememberCoroutineScope()

    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.isMoving }
            .distinctUntilChanged()
            .filter { moving -> !moving }
            .collect {
                cameraState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                    onCameraIdle(
                        MapViewport(
                            MapBounds(
                                south = bounds.southwest.latitude,
                                west = bounds.southwest.longitude,
                                north = bounds.northeast.latitude,
                                east = bounds.northeast.longitude,
                            ),
                            cameraState.position.zoom,
                        ),
                    )
                }
            }
    }

    LaunchedEffect(state.locationPermission) {
        if (state.locationPermission == LocationPermissionState.GRANTED) {
            lastKnownCoarseLocation(context)?.let { location ->
                onProgrammaticCameraMove()
                cameraState.animate(CameraUpdateFactory.newLatLngZoom(location, 13f))
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            if (hasRealMapKey) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize().testTag("explore_map"),
                    cameraPositionState = cameraState,
                    properties = MapProperties(
                        isMyLocationEnabled = state.locationPermission == LocationPermissionState.GRANTED,
                    ),
                    uiSettings = MapUiSettings(
                        compassEnabled = true,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false,
                    ),
                ) {
                    val clusterItems = remember(state.offers) { state.offers.map(::OfferClusterItem) }
                    Clustering(
                        items = clusterItems,
                        onClusterClick = { cluster ->
                            onProgrammaticCameraMove()
                            val clusterBounds = LatLngBounds.builder().apply {
                                cluster.items.forEach { include(it.position) }
                            }.build()
                            mapScope.launch {
                                cameraState.animate(CameraUpdateFactory.newLatLngBounds(clusterBounds, 96))
                            }
                            true
                        },
                        onClusterItemClick = { item ->
                            onSelect(item.offer.id)
                            true
                        },
                    )
                    state.selectedOffer?.let { selected ->
                        Marker(
                            state = rememberUpdatedMarkerState(
                                position = LatLng(
                                    selected.address.latitude.toDouble(),
                                    selected.address.longitude.toDouble(),
                                ),
                            ),
                            title = selected.title,
                            snippet = stringResource(R.string.selected_marker),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                            zIndex = 10f,
                            onClick = {
                                onSelect(selected.id)
                                true
                            },
                        )
                    }
                }
            } else {
                MapUnavailable(onOpenListFallback, Modifier.fillMaxSize())
            }

            ExploreSearchChrome(
                query = state.filter.query,
                onQuery = onQuery,
                onSubmit = onSubmitQuery,
                onFilters = onFilters,
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
            )

            IconButton(
                onClick = onLocate,
                modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
            ) {
                Icon(Icons.Outlined.GpsFixed, stringResource(R.string.use_my_location))
            }

            if (state.searchThisAreaVisible) {
                ElevatedButton(
                    enabled = !state.refreshing && !state.areaTooLarge,
                    onClick = onSearchArea,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 92.dp),
                ) {
                    if (state.refreshing) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(if (state.areaTooLarge) R.string.zoom_in_to_search else R.string.search_this_area))
                }
            }

            if (state.loading) LoadingMapOverlay(Modifier.align(Alignment.Center))
            if (state.offers.isEmpty() && !state.loading && state.error == null) {
                EmptyMapOverlay(Modifier.align(Alignment.Center))
            }
            if (state.error != null) {
                StaleErrorBanner(state.stale, onRetry, Modifier.align(Alignment.TopCenter).padding(top = 154.dp, start = 16.dp, end = 16.dp))
            }
            if (state.isTruncated) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 154.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 3.dp,
                ) {
                    Text(stringResource(R.string.area_truncated), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!expanded) {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    state.selectedOffer?.let { offer ->
                        SelectedOfferPreview(offer, { onOffer(offer.id) }, { onSelect(null) })
                        Spacer(Modifier.height(10.dp))
                    }
                    PresentationSwitch(ExplorePresentation.MAP, onList)
                }
            }
        }
        if (expanded) {
            Surface(
                modifier = Modifier.width(390.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
            ) {
                state.selectedOffer?.let { offer ->
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.selected_program), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        OfferCard(offer, { onOffer(offer.id) })
                        OutlinedButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.close))
                        }
                    }
                } ?: Column(
                    Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.explore_map_heading), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.select_marker_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    PresentationSwitch(ExplorePresentation.MAP, onList)
                }
            }
        }
    }
}

@Composable
private fun ListExplore(
    state: hu.ugorjbe.app.ui.viewmodel.ExploreUiState,
    onQuery: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onFilters: () -> Unit,
    onMap: () -> Unit,
    onOffer: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.explore_list_heading), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.results_in_map_area, state.offers.size), color = MaterialTheme.colorScheme.onSurfaceVariant)
            ExploreSearchChrome(state.filter.query, onQuery, onSubmitQuery, onFilters)
            AppliedFilterChips(state.filter)
            if (state.refreshing) CircularProgressIndicator(Modifier.fillMaxWidth().height(2.dp), strokeWidth = 2.dp)
        }
        when {
            state.loading -> LoadingPane(Modifier.weight(1f).fillMaxWidth())
            state.error != null && state.offers.isEmpty() -> ErrorPane(state.error, onRetry, Modifier.weight(1f).fillMaxWidth())
            state.offers.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_offers_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.no_offers_body), style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.stale) item { StaleErrorBanner(true, onRetry) }
                if (state.isTruncated) item {
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(16.dp)) {
                        Text(stringResource(R.string.area_truncated), Modifier.padding(12.dp))
                    }
                }
                items(state.offers, key = { it.id }) { offer -> OfferCard(offer, { onOffer(offer.id) }) }
            }
        }
        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
            PresentationSwitch(ExplorePresentation.LIST, onMap)
        }
    }
}

@Composable
private fun ExploreSearchChrome(
    query: String,
    onQuery: (String) -> Unit,
    onSubmit: () -> Unit,
    onFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), tonalElevation = 3.dp, shadowElevation = 3.dp) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            modifier = Modifier.fillMaxWidth().testTag("explore_search"),
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                Row {
                    IconButton(onClick = onSubmit) { Icon(Icons.Outlined.Search, stringResource(R.string.search_action)) }
                    IconButton(onClick = onFilters) { Icon(Icons.Outlined.FilterList, stringResource(R.string.filters)) }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
internal fun PresentationSwitch(current: ExplorePresentation, onSwitch: () -> Unit) {
    val targetList = current == ExplorePresentation.MAP
    ElevatedButton(
        onClick = onSwitch,
        shape = CircleShape,
        modifier = Modifier.testTag("explore_presentation_switch").semantics {
            role = Role.Button
            contentDescription = if (targetList) "Lista" else "Térkép"
        },
    ) {
        Icon(if (targetList) Icons.Outlined.List else Icons.Outlined.Map, null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(if (targetList) R.string.list_view else R.string.map_view))
    }
}

@Composable
private fun AppliedFilterChips(filter: OfferFilter) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filter.category?.let { category -> item { FilterChip(true, {}, { Text(stringResource(categoryLabel(category))) }) } }
        filter.childAge?.let { age -> item { FilterChip(true, {}, { Text(stringResource(R.string.age_filter_chip, age)) }) } }
        filter.maxPrice?.let { price -> item { FilterChip(true, {}, { Text(stringResource(R.string.price_under_huf, price.toInt())) }) } }
        item { FilterChip(true, {}, { Text(stringResource(R.string.places_filter_chip, filter.minAvailablePlaces)) }) }
    }
}

@Composable
private fun SelectedOfferPreview(offer: OfferSummary, onOpen: () -> Unit, onClose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("selected_offer_preview"),
        onClick = onOpen,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(offer.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${formatTime(offer.startsAtUtc)} · ${offer.provider.name}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${formatMoney(offer.discountedUnitPrice)} · ${stringResource(R.string.places_left, offer.availablePlaces)}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            TextButton(onClick = onOpen) { Text(stringResource(R.string.details)) }
            IconButton(onClick = onClose) { Text("×") }
        }
    }
}

@Composable
private fun MapUnavailable(onList: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Map, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.map_unavailable_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.map_unavailable_body), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onList) { Text(stringResource(R.string.open_list)) }
        }
    }
}

@Composable
private fun LoadingMapOverlay(modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), tonalElevation = 3.dp) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.loading_programs))
        }
    }
}

@Composable
private fun EmptyMapOverlay(modifier: Modifier = Modifier) {
    Surface(modifier.padding(24.dp), shape = RoundedCornerShape(20.dp), tonalElevation = 3.dp) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.no_offers_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.move_or_filter), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StaleErrorBanner(stale: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, null)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (stale) R.string.stale_results else R.string.error_network),
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Suppress("MissingPermission")
private fun lastKnownCoarseLocation(context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return null
    }
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return runCatching {
        manager.getProviders(true)
            .mapNotNull(manager::getLastKnownLocation)
            .maxByOrNull { it.time }
            ?.let { LatLng(it.latitude, it.longitude) }
    }.getOrNull()
}

@Composable
private fun FilterDialog(current: OfferFilter, onDismiss: () -> Unit, onApply: (OfferFilter) -> Unit) {
    var draft by remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filters)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text(stringResource(R.string.all_categories), fontWeight = FontWeight.SemiBold) }
                items(categories) { category ->
                    FilterChip(
                        selected = draft.category == category,
                        onClick = { draft = draft.copy(category = category) },
                        label = { Text(stringResource(category?.let(::categoryLabel) ?: R.string.all_categories)) },
                    )
                }
                item {
                    Text(stringResource(R.string.child_age), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 1).minus(1).coerceAtLeast(0)) }) { Text("−") }
                        Text(draft.childAge?.toString() ?: stringResource(R.string.any_age))
                        TextButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 0).plus(1).coerceAtMost(18)) }) { Text("+") }
                        if (draft.childAge != null) TextButton(onClick = { draft = draft.copy(childAge = null) }) { Text(stringResource(R.string.clear_filters)) }
                    }
                }
                item {
                    Text(stringResource(R.string.start_window), fontWeight = FontWeight.SemiBold)
                    listOf(
                        24 to R.string.start_next_day,
                        3 to R.string.start_next_three_hours,
                        6 to R.string.start_next_six_hours,
                    ).forEach { (hours, label) ->
                        FilterChip(
                            selected = draft.startsWithinHours == hours,
                            onClick = { draft = draft.copy(startsWithinHours = hours) },
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
                item {
                    Text(stringResource(R.string.maximum_price), fontWeight = FontWeight.SemiBold)
                    listOf<BigDecimal?>(null, BigDecimal("5000"), BigDecimal("10000")).forEach { price ->
                        FilterChip(
                            selected = draft.maxPrice == price,
                            onClick = { draft = draft.copy(maxPrice = price) },
                            label = {
                                Text(price?.let { stringResource(R.string.price_under_huf, it.toInt()) } ?: stringResource(R.string.any_price))
                            },
                        )
                    }
                }
                item {
                    Text(stringResource(R.string.minimum_places), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces - 1).coerceAtLeast(1)) }) { Text("−") }
                        Text(draft.minAvailablePlaces.toString())
                        TextButton(onClick = { draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces + 1).coerceAtMost(10)) }) { Text("+") }
                    }
                }
                item {
                    Text(stringResource(R.string.sort), fontWeight = FontWeight.SemiBold)
                    listOf(
                        "START_TIME" to R.string.sort_time,
                        "PRICE" to R.string.sort_price,
                        "DISCOUNT" to R.string.sort_discount,
                        "DISTANCE" to R.string.sort_distance,
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = draft.sort == value,
                            onClick = { draft = draft.copy(sort = value) },
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onApply(draft) }) { Text(stringResource(R.string.apply_filters)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
