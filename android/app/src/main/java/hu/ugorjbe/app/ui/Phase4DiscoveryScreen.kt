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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.TextStyle
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
import hu.ugorjbe.app.ui.theme.UgorjBeBrand
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

private val Phase4Budapest = LatLng(47.4979, 19.0402)
private val Phase4Categories = listOf<String?>(
    null,
    "PLAYHOUSE",
    "WORKSHOP",
    "MOVEMENT",
    "SWIMMING",
    "SPORT",
    "MUSEUM",
    "PARENT_CHILD",
)

private data class Phase4ClusterItem(val offer: OfferSummary) : ClusterItem {
    override fun getPosition() = LatLng(offer.address.latitude.toDouble(), offer.address.longitude.toDouble())
    override fun getTitle() = offer.title
    override fun getSnippet() = offer.provider.name
    override fun getZIndex() = 0f
}

@Composable
fun Phase4DiscoveryScreen(viewModel: DiscoveryViewModel, onOffer: (String) -> Unit) {
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
            (fadeIn(tween(UgorjBeMotion.Standard, easing = UgorjBeMotion.EnterEasing)) +
                scaleIn(initialScale = 0.99f)) togetherWith
                (fadeOut(tween(UgorjBeMotion.Quick, easing = UgorjBeMotion.ExitEasing)) +
                    scaleOut(targetScale = 1.01f))
        },
        label = "phase4-explore-presentation",
    ) { presentation ->
        when (presentation) {
            ExplorePresentation.MAP -> Phase4MapExplore(
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

            ExplorePresentation.LIST -> Phase4ListExplore(
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
        Phase4FilterSheet(
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
                }) {
                    Text(stringResource(R.string.open_settings))
                }
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
private fun Phase4MapExplore(
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
        position = CameraPosition.fromLatLngZoom(Phase4Budapest, 12f)
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
            phase4LastKnownLocation(context)?.let { location ->
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
                        val clusterItems = remember(state.offers) { state.offers.map(::Phase4ClusterItem) }
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
                                    color = MaterialTheme.colorScheme.secondary,
                                    border = BorderStroke(3.dp, Color.White),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            cluster.size.toString(),
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            },
                            clusterItemContent = { item ->
                                val selected = item.offer.id == state.selectedOfferId
                                Surface(
                                    shape = RoundedCornerShape(UgorjBeRadius.pill),
                                    color = if (selected) UgorjBeBrand.Coral else MaterialTheme.colorScheme.secondary,
                                    border = BorderStroke(3.dp, Color.White),
                                    shadowElevation = if (selected) 11.dp else 6.dp,
                                    modifier = Modifier.height(if (selected) 46.dp else 40.dp),
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = if (selected) 14.dp else 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Text(
                                            compactMoney(item.offer.discountedUnitPrice),
                                            color = Color.White,
                                            style = if (selected) {
                                                MaterialTheme.typography.labelLarge
                                            } else {
                                                MaterialTheme.typography.labelMedium
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            },
                        )
                    }
                } else {
                    Phase4MapUnavailable(onList, Modifier.fillMaxSize())
                }

                Column(
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = UgorjBeSpacing.lg, vertical = UgorjBeSpacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                ) {
                    Phase4SearchChrome(
                        query = state.filter.query,
                        activeFilters = activeFilterCount(state.filter),
                        onQuery = onQuery,
                        onSubmit = onSubmitQuery,
                        onFilters = onFilters,
                    )
                    Surface(
                        shape = RoundedCornerShape(UgorjBeRadius.pill),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Text(
                            stringResource(R.string.map_result_summary, state.offers.size),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.searchThisAreaVisible) {
                        Button(
                            enabled = !state.refreshing && !state.areaTooLarge,
                            onClick = onSearchArea,
                            shape = RoundedCornerShape(UgorjBeRadius.pill),
                        ) {
                            if (state.refreshing) {
                                CircularProgressIndicator(
                                    Modifier.size(17.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                stringResource(
                                    if (state.areaTooLarge) R.string.zoom_in_to_search else R.string.search_this_area,
                                ),
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(UgorjBeSpacing.lg),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 7.dp,
                ) {
                    IconButton(onClick = onLocate) {
                        Icon(
                            Icons.Outlined.GpsFixed,
                            stringResource(R.string.use_my_location),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                if (state.loading) {
                    Phase4LoadingMapOverlay(Modifier.align(Alignment.Center))
                }

                if (state.error != null) {
                    Phase4StaleBanner(
                        stale = state.stale,
                        onRetry = onRetry,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 120.dp, start = 16.dp, end = 16.dp),
                    )
                }

                if (state.offers.isEmpty() && !state.loading && state.error == null) {
                    Phase4MapEmpty(Modifier.align(Alignment.Center))
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
                            Phase4SelectedOfferCard(
                                offer = offer,
                                onOpen = { onOffer(offer.id) },
                                onClose = { onSelect(null) },
                            )
                        }
                        Phase4PresentationSwitch(ExplorePresentation.MAP, onList)
                    }
                }
            }

            if (expanded) {
                Surface(
                    modifier = Modifier.width(420.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        Modifier.padding(UgorjBeSpacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                    ) {
                        UgorjBeBrandMark(Modifier.size(48.dp))
                        Text(
                            stringResource(R.string.discover_eyebrow),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            stringResource(R.string.discover_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            stringResource(R.string.map_result_summary, state.offers.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.selectedOffer?.let { offer ->
                            OfferCard(offer, { onOffer(offer.id) })
                        } ?: Surface(
                            shape = RoundedCornerShape(UgorjBeRadius.large),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                stringResource(R.string.select_marker_hint),
                                modifier = Modifier.padding(UgorjBeSpacing.xl),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Phase4PresentationSwitch(ExplorePresentation.MAP, onList)
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase4ListExplore(
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
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = UgorjBeSpacing.xl, vertical = UgorjBeSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UgorjBeBrandMark(Modifier.size(44.dp), inverse = true)
                        Surface(
                            shape = RoundedCornerShape(UgorjBeRadius.pill),
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                stringResource(R.string.results_count, state.offers.size),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.list_editorial_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Text(
                        stringResource(R.string.list_editorial_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.78f),
                    )
                    Phase4SearchChrome(
                        query = state.filter.query,
                        activeFilters = activeFilterCount(state.filter),
                        onQuery = onQuery,
                        onSubmit = onSubmitQuery,
                        onFilters = onFilters,
                    )
                    Phase4AppliedFilters(state.filter)
                }
            }

            when {
                state.loading -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = UgorjBeSpacing.xl, vertical = UgorjBeSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    items(3) { OfferCardSkeleton() }
                }

                state.error != null && state.offers.isEmpty() -> ErrorPane(
                    state.error,
                    onRetry,
                    Modifier.weight(1f).fillMaxWidth(),
                )

                state.offers.isEmpty() -> Phase4ListEmpty(Modifier.weight(1f).fillMaxWidth())

                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().testTag("explore_list"),
                    contentPadding = PaddingValues(
                        start = UgorjBeSpacing.xl,
                        end = UgorjBeSpacing.xl,
                        top = UgorjBeSpacing.lg,
                        bottom = 112.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    if (state.stale) {
                        item { Phase4StaleBanner(true, onRetry) }
                    }
                    if (state.isTruncated) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(UgorjBeRadius.medium),
                            ) {
                                Text(
                                    stringResource(R.string.area_truncated),
                                    Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
                    items(state.offers, key = { it.id }) { offer ->
                        OfferCard(offer, { onOffer(offer.id) })
                    }
                }
            }
        }

        Phase4PresentationSwitch(
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
private fun Phase4SearchChrome(
    query: String,
    activeFilters: Int,
    onQuery: (String) -> Unit,
    onSubmit: () -> Unit,
    onFilters: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 9.dp,
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 7.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.weight(1f).testTag("explore_search"),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isBlank()) {
                            Text(
                                stringResource(R.string.search_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotBlank()) {
                IconButton(onClick = { onQuery("") }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable(onClick = onFilters),
            ) {
                Box {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Outlined.FilterList,
                            stringResource(R.string.filters),
                            Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondary,
                        )
                        Text(
                            stringResource(R.string.filters_short),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    }
                    if (activeFilters > 0) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).size(19.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    activeFilters.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase4PresentationSwitch(
    current: ExplorePresentation,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetList = current == ExplorePresentation.MAP
    Surface(
        modifier = modifier
            .testTag("explore_presentation_switch")
            .semantics {
                role = Role.Button
                contentDescription = if (targetList) "Lista" else "Térkép"
            }
            .clickable(onClick = onSwitch),
        shape = RoundedCornerShape(UgorjBeRadius.pill),
        color = MaterialTheme.colorScheme.secondary,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
        shadowElevation = 9.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                if (targetList) Icons.Outlined.List else Icons.Outlined.Map,
                null,
                tint = MaterialTheme.colorScheme.onSecondary,
            )
            Text(
                stringResource(if (targetList) R.string.list_view else R.string.map_view),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun Phase4SelectedOfferCard(
    offer: OfferSummary,
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("selected_offer_preview"),
        onClick = onOpen,
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 10.dp,
    ) {
        Row(
            Modifier.padding(UgorjBeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        ) {
            ExperienceImage(
                imageUrl = offer.imageUrl ?: offer.provider.imageUrl,
                category = offer.category,
                contentDescription = offer.title,
                modifier = Modifier.size(94.dp).clip(RoundedCornerShape(UgorjBeRadius.medium)),
            )
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    offer.provider.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${formatTime(offer.startsAtUtc)} · ${stringResource(R.string.availability_short, offer.availablePlaces)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatMoney(offer.discountedUnitPrice),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary) {
                    Icon(
                        Icons.Outlined.ArrowOutward,
                        stringResource(R.string.open_offer),
                        Modifier.padding(10.dp).size(19.dp),
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun Phase4LoadingMapOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.loading_programs))
        }
    }
}

@Composable
private fun Phase4MapEmpty(modifier: Modifier = Modifier) {
    Surface(
        modifier.padding(24.dp),
        shape = RoundedCornerShape(UgorjBeRadius.large),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
    ) {
        Column(
            Modifier.padding(UgorjBeSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm),
        ) {
            Phase3Lottie(R.raw.empty_discovery, Modifier.size(104.dp), loop = true) {
                Icon(
                    Icons.Outlined.Search,
                    null,
                    Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(stringResource(R.string.empty_discovery_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.move_or_filter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Phase4ListEmpty(modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        ) {
            Phase3Lottie(R.raw.empty_discovery, Modifier.size(150.dp), loop = true) {
                Icon(
                    Icons.Outlined.Search,
                    null,
                    Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(stringResource(R.string.empty_discovery_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.empty_discovery_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Phase4MapUnavailable(onList: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.md),
        ) {
            UgorjBeBrandMark(Modifier.size(62.dp))
            Icon(Icons.Outlined.Map, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(stringResource(R.string.map_unavailable_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.map_unavailable_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onList) { Text(stringResource(R.string.open_list)) }
        }
    }
}

@Composable
private fun Phase4StaleBanner(
    stale: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(UgorjBeRadius.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        shadowElevation = 4.dp,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (stale) R.string.stale_results else R.string.error_network),
                Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun Phase4AppliedFilters(filter: OfferFilter) {
    val entries = buildList<Pair<String, String>> {
        filter.category?.let { add("category" to it) }
        filter.childAge?.let { add("age" to it.toString()) }
        filter.maxPrice?.let { add("price" to it.toPlainString()) }
    }
    if (entries.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.first }) { (type, value) ->
            Surface(
                shape = RoundedCornerShape(UgorjBeRadius.pill),
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.12f)),
            ) {
                Text(
                    text = when (type) {
                        "category" -> stringResource(categoryLabel(value))
                        "age" -> stringResource(R.string.age_filter_chip, value.toInt())
                        else -> stringResource(R.string.price_under_huf, value.toBigDecimal().toInt())
                    },
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Phase4FilterSheet(
    current: OfferFilter,
    onDismiss: () -> Unit,
    onApply: (OfferFilter) -> Unit,
) {
    var draft by remember(current) { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = UgorjBeSpacing.xl,
                end = UgorjBeSpacing.xl,
                bottom = 36.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.filters), style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(R.string.discover_subtitle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { draft = OfferFilter(query = draft.query) }) {
                        Text(stringResource(R.string.clear_filters))
                    }
                }
            }

            item { HorizontalDivider() }
            item { FilterSectionTitle(stringResource(R.string.all_categories)) }
            items(Phase4Categories, key = { it ?: "all" }) { category ->
                FilterChip(
                    selected = draft.category == category,
                    onClick = { draft = draft.copy(category = category) },
                    label = {
                        Text(stringResource(category?.let(::categoryLabel) ?: R.string.all_categories))
                    },
                )
            }

            item { FilterSectionTitle(stringResource(R.string.child_age)) }
            item {
                CounterControl(
                    value = draft.childAge,
                    emptyLabel = stringResource(R.string.any_age),
                    onDecrease = {
                        draft = draft.copy(childAge = (draft.childAge ?: 1).minus(1).coerceAtLeast(0))
                    },
                    onIncrease = {
                        draft = draft.copy(childAge = (draft.childAge ?: 0).plus(1).coerceAtMost(18))
                    },
                    onClear = { draft = draft.copy(childAge = null) },
                )
            }

            item { FilterSectionTitle(stringResource(R.string.start_window)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        3 to R.string.start_next_three_hours,
                        6 to R.string.start_next_six_hours,
                        24 to R.string.start_next_day,
                    ).forEach { (hours, label) ->
                        FilterChip(
                            selected = draft.startsWithinHours == hours,
                            onClick = { draft = draft.copy(startsWithinHours = hours) },
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
            }

            item { FilterSectionTitle(stringResource(R.string.maximum_price)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf<BigDecimal?>(null, BigDecimal("5000"), BigDecimal("10000")).forEach { price ->
                        FilterChip(
                            selected = draft.maxPrice == price,
                            onClick = { draft = draft.copy(maxPrice = price) },
                            label = {
                                Text(
                                    price?.let {
                                        stringResource(R.string.price_under_huf, it.toInt())
                                    } ?: stringResource(R.string.any_price),
                                )
                            },
                        )
                    }
                }
            }

            item { FilterSectionTitle(stringResource(R.string.minimum_places)) }
            item {
                CounterControl(
                    value = draft.minAvailablePlaces,
                    emptyLabel = "1",
                    onDecrease = {
                        draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces - 1).coerceAtLeast(1))
                    },
                    onIncrease = {
                        draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces + 1).coerceAtMost(10))
                    },
                    onClear = null,
                )
            }

            item { FilterSectionTitle(stringResource(R.string.sort)) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            item {
                Button(
                    onClick = { onApply(draft) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(UgorjBeRadius.medium),
                ) {
                    Text(stringResource(R.string.apply_filters))
                }
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun CounterControl(
    value: Int?,
    emptyLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onClear: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(UgorjBeRadius.medium),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onDecrease, shape = CircleShape) { Text("−") }
            Text(
                value?.toString() ?: emptyLabel,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                onClear?.let {
                    TextButton(onClick = it) { Text(stringResource(R.string.clear_filters)) }
                }
                OutlinedButton(onClick = onIncrease, shape = CircleShape) { Text("+") }
            }
        }
    }
}

private fun activeFilterCount(filter: OfferFilter): Int = listOfNotNull(
    filter.category,
    filter.childAge,
    filter.maxPrice,
    filter.startsWithinHours?.takeIf { it != 24 },
    filter.minAvailablePlaces.takeIf { it != 1 },
    filter.sort.takeIf { it != "START_TIME" },
).size

@Suppress("MissingPermission")
private fun phase4LastKnownLocation(context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
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
