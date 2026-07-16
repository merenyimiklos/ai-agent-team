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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.MapViewport
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.ui.theme.UgorjBeMotion
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
import hu.ugorjbe.app.ui.viewmodel.DiscoveryViewModel
import hu.ugorjbe.app.ui.viewmodel.ExplorePresentation
import hu.ugorjbe.app.ui.viewmodel.ExploreUiState
import hu.ugorjbe.app.ui.viewmodel.LocationPermissionState
import java.math.BigDecimal
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private val Phase3Budapest = LatLng(47.4979, 19.0402)
private val Phase3Categories = listOf<String?>(
    null,
    "PLAYHOUSE",
    "WORKSHOP",
    "MOVEMENT",
    "SWIMMING",
    "SPORT",
    "MUSEUM",
    "PARENT_CHILD",
)

private data class Phase3ClusterItem(val offer: OfferSummary) : ClusterItem {
    override fun getPosition() = LatLng(offer.address.latitude.toDouble(), offer.address.longitude.toDouble())
    override fun getTitle() = offer.title
    override fun getSnippet() = offer.provider.name
    override fun getZIndex() = 0f
}

@Composable
fun Phase3DiscoveryScreen(viewModel: DiscoveryViewModel, onOffer: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val permanentlyDenied = !granted && activity?.let {
            !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION)
        } == true
        viewModel.onLocationPermissionResult(granted, permanentlyDenied)
    }

    AnimatedContent(
        targetState = state.presentation,
        transitionSpec = {
            (fadeIn(tween(UgorjBeMotion.Standard)) + scaleIn(initialScale = 0.985f)) togetherWith
                (fadeOut(tween(UgorjBeMotion.Quick)) + scaleOut(targetScale = 1.015f))
        },
        label = "explore-presentation",
    ) { presentation ->
        when (presentation) {
            ExplorePresentation.MAP -> Phase3MapExplore(
                state = state,
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
            )
            ExplorePresentation.LIST -> Phase3ListExplore(
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
        Phase3FilterSheet(
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
private fun Phase3MapExplore(
    state: ExploreUiState,
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
) {
    val context = LocalContext.current
    val hasMapKey = BuildConfig.MAPS_API_KEY.isNotBlank() && BuildConfig.MAPS_API_KEY != "DEFAULT_API_KEY"
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Phase3Budapest, 12f)
    }
    val mapScope = rememberCoroutineScope()

    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.isMoving }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                cameraState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                    onCameraIdle(
                        MapViewport(
                            bounds = MapBounds(
                                south = bounds.southwest.latitude,
                                west = bounds.southwest.longitude,
                                north = bounds.northeast.latitude,
                                east = bounds.northeast.longitude,
                            ),
                            zoom = cameraState.position.zoom,
                        ),
                    )
                }
            }
    }

    LaunchedEffect(state.locationPermission) {
        if (state.locationPermission == LocationPermissionState.GRANTED) {
            phase3LastKnownLocation(context)?.let { location ->
                onProgrammaticCameraMove()
                cameraState.animate(CameraUpdateFactory.newLatLngZoom(location, 13.5f))
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if (hasMapKey) {
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
                        onMapClick = { onSelect(null) },
                    ) {
                        val clusterItems = remember(state.offers) { state.offers.map(::Phase3ClusterItem) }
                        Clustering(
                            items = clusterItems,
                            onClusterClick = { cluster ->
                                onProgrammaticCameraMove()
                                val bounds = LatLngBounds.builder().apply {
                                    cluster.items.forEach { include(it.position) }
                                }.build()
                                mapScope.launch {
                                    cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 104))
                                }
                                true
                            },
                            onClusterItemClick = { item ->
                                onSelect(item.offer.id)
                                true
                            },
                            clusterContent = { cluster ->
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    shadowElevation = 7.dp,
                                    modifier = Modifier.size(46.dp).border(3.dp, Color.White, CircleShape),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            cluster.size.toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            },
                            clusterItemContent = { item ->
                                val selected = item.offer.id == state.selectedOfferId
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    shadowElevation = if (selected) 10.dp else 5.dp,
                                    modifier = Modifier
                                        .size(if (selected) 48.dp else 40.dp)
                                        .border(3.dp, Color.White, CircleShape),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = item.offer.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(if (selected) 26.dp else 22.dp),
                                        )
                                    }
                                }
                            },
                        )
                    }
                } else {
                    Phase3MapUnavailable(onList, Modifier.fillMaxSize())
                }

                Column(
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = UgorjBeSpacing.lg, vertical = UgorjBeSpacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                ) {
                    Phase3SearchChrome(
                        query = state.filter.query,
                        activeFilters = activeFilterCount(state.filter),
                        onQuery = onQuery,
                        onSubmit = onSubmitQuery,
                        onFilters = onFilters,
                    )
                    if (state.searchThisAreaVisible) {
                        ElevatedButton(
                            enabled = !state.refreshing && !state.areaTooLarge,
                            onClick = onSearchArea,
                            shape = RoundedCornerShape(UgorjBeRadius.pill),
                        ) {
                            if (state.refreshing) {
                                CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(stringResource(if (state.areaTooLarge) R.string.zoom_in_to_search else R.string.search_this_area))
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(UgorjBeSpacing.lg),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 7.dp,
                ) {
                    IconButton(onClick = onLocate) {
                        Icon(Icons.Outlined.GpsFixed, stringResource(R.string.use_my_location))
                    }
                }

                if (state.loading) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = RoundedCornerShape(UgorjBeRadius.large),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        shadowElevation = 8.dp,
                    ) {
                        Row(
                            Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.loading_programs))
                        }
                    }
                }

                if (state.error != null) {
                    Phase3StaleBanner(
                        stale = state.stale,
                        onRetry = onRetry,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 104.dp, start = 16.dp, end = 16.dp),
                    )
                }

                if (state.offers.isEmpty() && !state.loading && state.error == null) {
                    Phase3MapEmpty(Modifier.align(Alignment.Center))
                }

                if (!expanded) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = UgorjBeSpacing.lg, vertical = UgorjBeSpacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                    ) {
                        state.selectedOffer?.let { offer ->
                            Phase3SelectedOfferCard(
                                offer = offer,
                                onOpen = { onOffer(offer.id) },
                                onClose = { onSelect(null) },
                            )
                        }
                        Phase3PresentationSwitch(ExplorePresentation.MAP, onList)
                    }
                }
            }

            if (expanded) {
                Surface(
                    modifier = Modifier.width(410.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        Modifier.padding(UgorjBeSpacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                    ) {
                        Text(stringResource(R.string.discover_eyebrow), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.discover_title), style = MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(R.string.results_count, state.offers.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.selectedOffer?.let { offer ->
                            OfferCard(offer, { onOffer(offer.id) })
                        } ?: Text(
                            stringResource(R.string.select_marker_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Phase3PresentationSwitch(ExplorePresentation.MAP, onList)
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase3ListExplore(
    state: ExploreUiState,
    onQuery: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onFilters: () -> Unit,
    onMap: () -> Unit,
    onOffer: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.statusBarsPadding().padding(horizontal = UgorjBeSpacing.xl, vertical = UgorjBeSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
            ) {
                Text(stringResource(R.string.discover_eyebrow), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.discover_title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.discover_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Phase3SearchChrome(
                    query = state.filter.query,
                    activeFilters = activeFilterCount(state.filter),
                    onQuery = onQuery,
                    onSubmit = onSubmitQuery,
                    onFilters = onFilters,
                )
                Phase3AppliedFilters(state.filter)
            }

            when {
                state.loading -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = UgorjBeSpacing.xl, vertical = UgorjBeSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    items(3) { OfferCardSkeleton() }
                }
                state.error != null && state.offers.isEmpty() -> ErrorPane(state.error, onRetry, Modifier.weight(1f).fillMaxWidth())
                state.offers.isEmpty() -> Phase3ListEmpty(Modifier.weight(1f).fillMaxWidth())
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = UgorjBeSpacing.xl,
                        end = UgorjBeSpacing.xl,
                        top = UgorjBeSpacing.sm,
                        bottom = 112.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(R.string.results_count, state.offers.size), style = MaterialTheme.typography.titleMedium)
                            if (state.refreshing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                    if (state.stale) item { Phase3StaleBanner(true, onRetry) }
                    items(state.offers, key = { it.id }) { offer ->
                        OfferCard(offer, { onOffer(offer.id) })
                    }
                }
            }
        }

        Phase3PresentationSwitch(
            current = ExplorePresentation.LIST,
            onSwitch = onMap,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = UgorjBeSpacing.lg),
        )
    }
}

@Composable
private fun Phase3SearchChrome(
    query: String,
    activeFilters: Int,
    onQuery: (String) -> Unit,
    onSubmit: () -> Unit,
    onFilters: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        shadowElevation = 10.dp,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            placeholder = { Text(stringResource(R.string.search_hint), maxLines = 1) },
            modifier = Modifier.fillMaxWidth().testTag("explore_search"),
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSubmit) {
                        Icon(Icons.Outlined.Search, stringResource(R.string.search_action))
                    }
                    Box {
                        IconButton(onClick = onFilters) {
                            Icon(Icons.Outlined.FilterList, stringResource(R.string.filters))
                        }
                        if (activeFilters > 0) {
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(activeFilters.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            singleLine = true,
            shape = RoundedCornerShape(UgorjBeRadius.large),
        )
    }
}

@Composable
private fun Phase3PresentationSwitch(
    current: ExplorePresentation,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetList = current == ExplorePresentation.MAP
    ElevatedButton(
        onClick = onSwitch,
        shape = RoundedCornerShape(UgorjBeRadius.pill),
        modifier = modifier
            .testTag("explore_presentation_switch")
            .semantics {
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
private fun Phase3SelectedOfferCard(offer: OfferSummary, onOpen: () -> Unit, onClose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("selected_offer_preview"),
        onClick = onOpen,
        shape = RoundedCornerShape(UgorjBeRadius.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Row(Modifier.padding(UgorjBeSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            ExperienceImage(
                imageUrl = offer.imageUrl ?: offer.provider.imageUrl,
                category = offer.category,
                contentDescription = offer.title,
                modifier = Modifier.size(92.dp).clip(RoundedCornerShape(UgorjBeRadius.medium)),
            )
            Column(
                Modifier.weight(1f).padding(horizontal = UgorjBeSpacing.md),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(offer.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatTime(offer.startsAtUtc)} · ${offer.provider.name}", style = MaterialTheme.typography.bodySmall)
                Text(formatMoney(offer.discountedUnitPrice), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, stringResource(R.string.close)) }
        }
    }
}

@Composable
private fun Phase3MapEmpty(modifier: Modifier = Modifier) {
    Surface(modifier.padding(24.dp), shape = RoundedCornerShape(UgorjBeRadius.large), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f), shadowElevation = 8.dp) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Phase3Lottie(R.raw.empty_discovery, Modifier.size(104.dp), loop = true) {
                Icon(Icons.Outlined.Search, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text(stringResource(R.string.empty_discovery_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.move_or_filter), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Phase3ListEmpty(modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md)) {
            Phase3Lottie(R.raw.empty_discovery, Modifier.size(150.dp), loop = true) {
                Icon(Icons.Outlined.Search, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text(stringResource(R.string.empty_discovery_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.empty_discovery_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Phase3MapUnavailable(onList: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        ) {
            Icon(Icons.Outlined.Map, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.map_unavailable_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.map_unavailable_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onList) { Text(stringResource(R.string.open_list)) }
        }
    }
}

@Composable
private fun Phase3StaleBanner(stale: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(UgorjBeRadius.medium)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (stale) R.string.stale_results else R.string.error_network),
                Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun Phase3AppliedFilters(filter: OfferFilter) {
    val entries = buildList<Pair<String, String>> {
        filter.category?.let { add("category" to it) }
        filter.childAge?.let { add("age" to it.toString()) }
        filter.maxPrice?.let { add("price" to it.toPlainString()) }
    }
    if (entries.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.first }) { (type, value) ->
            Surface(shape = RoundedCornerShape(UgorjBeRadius.pill), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = when (type) {
                        "category" -> stringResource(categoryLabel(value))
                        "age" -> stringResource(R.string.age_filter_chip, value.toInt())
                        else -> stringResource(R.string.price_under_huf, value.toBigDecimal().toInt())
                    },
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Phase3FilterSheet(current: OfferFilter, onDismiss: () -> Unit, onApply: (OfferFilter) -> Unit) {
    var draft by remember(current) { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = UgorjBeSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.filters), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = {
                    draft = OfferFilter(query = current.query)
                }) { Text(stringResource(R.string.clear_filters)) }
            }
            Text(stringResource(R.string.all_categories), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Phase3Categories) { category ->
                    FilterChip(
                        selected = draft.category == category,
                        onClick = { draft = draft.copy(category = category) },
                        label = { Text(stringResource(category?.let(::categoryLabel) ?: R.string.all_categories)) },
                    )
                }
            }
            Text(stringResource(R.string.child_age), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 1).minus(1).coerceAtLeast(0)) }) { Text("−") }
                Text(draft.childAge?.toString() ?: stringResource(R.string.any_age), style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 0).plus(1).coerceAtMost(18)) }) { Text("+") }
                if (draft.childAge != null) TextButton(onClick = { draft = draft.copy(childAge = null) }) { Text(stringResource(R.string.clear_filters)) }
            }
            Text(stringResource(R.string.start_window), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3 to R.string.start_next_three_hours, 6 to R.string.start_next_six_hours, 24 to R.string.start_next_day).forEach { (hours, label) ->
                    FilterChip(
                        selected = draft.startsWithinHours == hours,
                        onClick = { draft = draft.copy(startsWithinHours = hours) },
                        label = { Text(stringResource(label)) },
                    )
                }
            }
            Text(stringResource(R.string.maximum_price), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf<BigDecimal?>(null, BigDecimal("5000"), BigDecimal("10000")).forEach { price ->
                    FilterChip(
                        selected = draft.maxPrice == price,
                        onClick = { draft = draft.copy(maxPrice = price) },
                        label = { Text(price?.let { stringResource(R.string.price_under_huf, it.toInt()) } ?: stringResource(R.string.any_price)) },
                    )
                }
            }
            Text(stringResource(R.string.sort), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        "START_TIME" to R.string.sort_time,
                        "PRICE" to R.string.sort_price,
                        "DISCOUNT" to R.string.sort_discount,
                        "DISTANCE" to R.string.sort_distance,
                    ),
                ) { (value, label) ->
                    FilterChip(
                        selected = draft.sort == value,
                        onClick = { draft = draft.copy(sort = value) },
                        label = { Text(stringResource(label)) },
                    )
                }
            }
            Button(
                onClick = { onApply(draft) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(UgorjBeRadius.medium),
            ) {
                Text(stringResource(R.string.apply_filters))
            }
            Spacer(Modifier.height(UgorjBeSpacing.md))
        }
    }
}

private fun activeFilterCount(filter: OfferFilter): Int = listOfNotNull(
    filter.category,
    filter.childAge,
    filter.maxPrice,
    filter.startsWithinHours?.takeIf { it != 24 },
    filter.sort.takeIf { it != "START_TIME" },
    filter.minAvailablePlaces.takeIf { it != 1 },
).size

@Suppress("MissingPermission")
private fun phase3LastKnownLocation(context: Context): LatLng? {
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
