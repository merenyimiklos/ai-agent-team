package hu.ugorjbe.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.AuthRepository
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.BookingRepository
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.FavoritesRepository
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.MapViewport
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.domain.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    sessionStore: SessionStore,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val session: StateFlow<Session?> = sessionStore.session.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    fun logout() = viewModelScope.launch { authRepository.logout() }
}

enum class AuthMode { LOGIN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val submitting: Boolean = false,
    val error: ApiError? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    val state = MutableStateFlow(AuthUiState())

    fun setEmail(value: String) = state.update { it.copy(email = value, error = null) }
    fun setPassword(value: String) = state.update { it.copy(password = value, error = null) }
    fun setDisplayName(value: String) = state.update { it.copy(displayName = value, error = null) }
    fun toggleMode() = state.update {
        it.copy(
            mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
            error = null,
        )
    }

    fun submit() {
        val current = state.value
        if (current.email.isBlank() || current.password.isBlank() ||
            (current.mode == AuthMode.REGISTER && current.displayName.isBlank())
        ) {
            state.update { it.copy(error = ApiError(ApiError.Kind.VALIDATION)) }
            return
        }
        viewModelScope.launch {
            state.update { it.copy(submitting = true, error = null) }
            val result = if (current.mode == AuthMode.LOGIN) {
                repository.login(current.email, current.password)
            } else {
                repository.register(current.email, current.password, current.displayName)
            }
            state.update {
                when (result) {
                    is ApiResult.Success -> it.copy(submitting = false)
                    is ApiResult.Failure -> it.copy(submitting = false, error = result.error)
                }
            }
        }
    }
}

enum class ExplorePresentation { MAP, LIST }

enum class LocationPermissionState { NOT_REQUESTED, RATIONALE, GRANTED, DENIED, PERMANENTLY_DENIED }

data class ExploreUiState(
    val presentation: ExplorePresentation = ExplorePresentation.MAP,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val offers: List<OfferSummary> = emptyList(),
    val filter: OfferFilter = OfferFilter(),
    val filterDraft: OfferFilter? = null,
    val searchedViewport: MapViewport = MapViewport(MapBounds.Budapest, 12f),
    val currentViewport: MapViewport = searchedViewport,
    val selectedOfferId: String? = null,
    val isTruncated: Boolean = false,
    val searchThisAreaVisible: Boolean = false,
    val areaTooLarge: Boolean = false,
    val stale: Boolean = false,
    val error: ApiError? = null,
    val locationPermission: LocationPermissionState = LocationPermissionState.NOT_REQUESTED,
    val ignoreNextCameraIdle: Boolean = true,
    val requestGeneration: Long = 0,
) {
    val selectedOffer: OfferSummary? get() = offers.firstOrNull { it.id == selectedOfferId }
}

object MapSearchPolicy {
    fun shouldOfferSearch(searched: MapViewport, visible: MapViewport): Boolean {
        val movedHorizontally = kotlin.math.abs(searched.bounds.centerLongitude - visible.bounds.centerLongitude) >=
            visible.bounds.longitudeSpan * 0.15
        val movedVertically = kotlin.math.abs(searched.bounds.centerLatitude - visible.bounds.centerLatitude) >=
            visible.bounds.latitudeSpan * 0.15
        val zoomChanged = kotlin.math.abs(searched.zoom - visible.zoom) >= 0.5f
        return movedHorizontally || movedVertically || zoomChanged || intersectionOverUnion(
            searched.bounds,
            visible.bounds,
        ) < 0.8
    }

    fun intersectionOverUnion(first: MapBounds, second: MapBounds): Double {
        val intersectionWidth = (minOf(first.east, second.east) - maxOf(first.west, second.west)).coerceAtLeast(0.0)
        val intersectionHeight = (minOf(first.north, second.north) - maxOf(first.south, second.south)).coerceAtLeast(0.0)
        val intersection = intersectionWidth * intersectionHeight
        val firstArea = first.longitudeSpan * first.latitudeSpan
        val secondArea = second.longitudeSpan * second.latitudeSpan
        val union = firstArea + secondArea - intersection
        return if (union <= 0.0) 0.0 else intersection / union
    }
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    val state = MutableStateFlow(
        ExploreUiState(
            presentation = runCatching {
                ExplorePresentation.valueOf(savedStateHandle["explorePresentation"] ?: "MAP")
            }.getOrDefault(ExplorePresentation.MAP),
            filter = OfferFilter(query = savedStateHandle["exploreQuery"] ?: ""),
        ),
    )
    private var loadJob: Job? = null

    init { search(state.value.searchedViewport) }

    fun setQuery(value: String) = state.update { it.copy(filter = it.filter.copy(query = value)) }

    fun submitQuery() {
        savedStateHandle["exploreQuery"] = state.value.filter.query
        search(state.value.currentViewport)
    }

    fun openFilters() = state.update { it.copy(filterDraft = it.filter) }
    fun updateFilterDraft(filter: OfferFilter) = state.update { it.copy(filterDraft = filter) }
    fun dismissFilters() = state.update { it.copy(filterDraft = null) }

    fun applyFilter(filter: OfferFilter) {
        state.update { it.copy(filter = filter, filterDraft = null) }
        savedStateHandle["exploreQuery"] = filter.query
        search(state.value.currentViewport)
    }

    fun setPresentation(presentation: ExplorePresentation) {
        state.update { it.copy(presentation = presentation) }
        savedStateHandle["explorePresentation"] = presentation.name
    }

    fun selectOffer(id: String?) = state.update { current ->
        current.copy(selectedOfferId = id?.takeIf { selected -> current.offers.any { it.id == selected } })
    }

    fun onProgrammaticCameraMove() = state.update { it.copy(ignoreNextCameraIdle = true) }

    fun onCameraIdle(viewport: MapViewport) = state.update { current ->
        if (current.ignoreNextCameraIdle) {
            current.copy(currentViewport = viewport, ignoreNextCameraIdle = false, searchThisAreaVisible = false)
        } else {
            current.copy(
                currentViewport = viewport,
                areaTooLarge = !viewport.bounds.isApiValid(),
                searchThisAreaVisible = MapSearchPolicy.shouldOfferSearch(current.searchedViewport, viewport),
            )
        }
    }

    fun searchThisArea() {
        if (state.value.currentViewport.bounds.isApiValid()) search(state.value.currentViewport)
    }

    fun refresh() = search(state.value.searchedViewport)
    fun retry() = search(state.value.currentViewport)

    fun requestLocation() = state.update { current ->
        if (current.locationPermission != LocationPermissionState.GRANTED) {
            current.copy(locationPermission = LocationPermissionState.RATIONALE)
        } else current
    }

    fun dismissLocationRationale() = state.update { it.copy(locationPermission = LocationPermissionState.DENIED) }

    fun onLocationPermissionResult(granted: Boolean, permanentlyDenied: Boolean = false) = state.update {
        it.copy(
            locationPermission = when {
                granted -> LocationPermissionState.GRANTED
                permanentlyDenied -> LocationPermissionState.PERMANENTLY_DENIED
                else -> LocationPermissionState.DENIED
            },
        )
    }

    private fun search(viewport: MapViewport) {
        if (!viewport.bounds.isApiValid()) {
            state.update { it.copy(areaTooLarge = true, searchThisAreaVisible = true) }
            return
        }
        val requestedFilter = state.value.filter
        val generation = state.value.requestGeneration + 1
        loadJob?.cancel()
        state.update {
            it.copy(
                loading = it.offers.isEmpty(),
                refreshing = it.offers.isNotEmpty(),
                error = null,
                areaTooLarge = false,
                requestGeneration = generation,
            )
        }
        loadJob = viewModelScope.launch {
            val result = repository.mapOffers(viewport.bounds, requestedFilter)
            if (state.value.requestGeneration != generation || state.value.filter != requestedFilter) return@launch
            when (result) {
                is ApiResult.Success -> state.update { current ->
                    val items = result.value.items
                    current.copy(
                        loading = false,
                        refreshing = false,
                        offers = items,
                        searchedViewport = viewport,
                        currentViewport = viewport,
                        selectedOfferId = current.selectedOfferId?.takeIf { id -> items.any { it.id == id } },
                        isTruncated = result.value.isTruncated,
                        searchThisAreaVisible = false,
                        stale = false,
                        error = null,
                    )
                }
                is ApiResult.Failure -> state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        stale = it.offers.isNotEmpty(),
                        error = result.error,
                    )
                }
            }
        }
    }
}

typealias DiscoveryViewModel = ExploreViewModel

data class OfferDetailUiState(
    val loading: Boolean = true,
    val offer: OfferDetail? = null,
    val quantity: Int = 1,
    val favorite: Boolean = false,
    val reserving: Boolean = false,
    val booking: Booking? = null,
    val error: ApiError? = null,
    val message: ApiError? = null,
)

@HiltViewModel
class OfferDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val bookings: BookingRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val offerId: String = checkNotNull(savedStateHandle["offerId"])
    val state = MutableStateFlow(OfferDetailUiState())

    init { load() }

    fun load(message: ApiError? = null) = viewModelScope.launch {
        state.update { it.copy(loading = true, error = null, message = message) }
        val offerResult = catalog.offer(offerId)
        if (offerResult is ApiResult.Failure) {
            state.update { it.copy(loading = false, error = offerResult.error) }
            return@launch
        }
        val offer = (offerResult as ApiResult.Success).value
        val favorite = when (val favoriteResult = favorites.offers()) {
            is ApiResult.Success -> favoriteResult.value.items.any { it.id == offerId }
            is ApiResult.Failure -> false
        }
        state.update {
            it.copy(
                loading = false,
                offer = offer,
                favorite = favorite,
                quantity = it.quantity.coerceIn(1, offer.availablePlaces.coerceAtLeast(1)),
            )
        }
    }

    fun setQuantity(value: Int) = state.update { current ->
        current.copy(quantity = value.coerceIn(1, current.offer?.availablePlaces?.coerceAtLeast(1) ?: 1))
    }

    fun toggleFavorite() = viewModelScope.launch {
        val target = !state.value.favorite
        when (val result = favorites.setOfferFavorite(offerId, target)) {
            is ApiResult.Success -> state.update { it.copy(favorite = target, message = null) }
            is ApiResult.Failure -> state.update { it.copy(message = result.error) }
        }
    }

    fun reserve() {
        if (state.value.reserving || state.value.offer?.isBookable != true) return
        viewModelScope.launch {
            state.update { it.copy(reserving = true, message = null) }
            when (val result = bookings.create(offerId, state.value.quantity)) {
                is ApiResult.Success -> state.update {
                    it.copy(reserving = false, booking = result.value)
                }
                is ApiResult.Failure -> when (result.error.kind) {
                    ApiError.Kind.INSUFFICIENT_CAPACITY -> {
                        val available = result.error.availablePlaces ?: 1
                        state.update {
                            it.copy(
                                reserving = false,
                                quantity = available.coerceAtLeast(1),
                                offer = it.offer?.copy(availablePlaces = available, isBookable = available > 0),
                                message = result.error,
                            )
                        }
                    }
                    ApiError.Kind.OFFER_NOT_BOOKABLE -> {
                        state.update { it.copy(reserving = false) }
                        load(result.error)
                    }
                    else -> state.update { it.copy(reserving = false, message = result.error) }
                }
            }
        }
    }

    fun consumeBooking() = state.update { it.copy(booking = null) }
    fun dismissMessage() = state.update { it.copy(message = null) }
}

data class BookingsUiState(
    val scope: String = "ACTIVE",
    val loading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val cancellingId: String? = null,
    val error: ApiError? = null,
    val message: ApiError? = null,
)

@HiltViewModel
class BookingsViewModel @Inject constructor(private val repository: BookingRepository) : ViewModel() {
    val state = MutableStateFlow(BookingsUiState())
    private var loadJob: Job? = null

    init { refresh() }

    fun setScope(scope: String) {
        state.update { it.copy(scope = scope) }
        refresh()
    }

    fun refresh() {
        val requestedScope = state.value.scope
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(loading = true, cancellingId = null, error = null) }
            val result = repository.list(requestedScope)
            if (state.value.scope != requestedScope) return@launch
            when (result) {
            is ApiResult.Success -> state.update {
                it.copy(loading = false, bookings = result.value.items, error = null)
            }
            is ApiResult.Failure -> state.update { it.copy(loading = false, error = result.error) }
            }
        }
    }

    fun cancel(id: String) = viewModelScope.launch {
        state.update { it.copy(cancellingId = id, message = null) }
        when (val result = repository.cancel(id)) {
            is ApiResult.Success -> refresh()
            is ApiResult.Failure -> {
                state.update { it.copy(cancellingId = null, message = result.error) }
                if (result.error.kind == ApiError.Kind.CANCELLATION_NOT_ALLOWED) refresh()
            }
        }
    }

    fun dismissMessage() = state.update { it.copy(message = null) }
}

data class FavoritesUiState(
    val loading: Boolean = true,
    val offers: List<OfferSummary> = emptyList(),
    val providers: List<ProviderSummary> = emptyList(),
    val error: ApiError? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: FavoritesRepository) : ViewModel() {
    val state = MutableStateFlow(FavoritesUiState())

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        state.update { it.copy(loading = true, error = null) }
        val offerResult = async { repository.offers() }
        val providerResult = async { repository.providers() }
        val offers = offerResult.await()
        val providers = providerResult.await()
        val error = (offers as? ApiResult.Failure)?.error ?: (providers as? ApiResult.Failure)?.error
        state.update {
            it.copy(
                loading = false,
                offers = (offers as? ApiResult.Success)?.value?.items ?: emptyList(),
                providers = (providers as? ApiResult.Success)?.value?.items ?: emptyList(),
                error = error,
            )
        }
    }
}

data class ProviderUiState(
    val loading: Boolean = true,
    val provider: ProviderDetail? = null,
    val favorite: Boolean = false,
    val error: ApiError? = null,
)

@HiltViewModel
class ProviderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val providerId: String = checkNotNull(savedStateHandle["providerId"])
    val state = MutableStateFlow(ProviderUiState())

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        state.update { it.copy(loading = true, error = null) }
        when (val result = catalog.provider(providerId)) {
            is ApiResult.Failure -> state.update { it.copy(loading = false, error = result.error) }
            is ApiResult.Success -> {
                val favorite = (favorites.providers() as? ApiResult.Success)?.value?.items?.any { it.id == providerId } == true
                state.update { it.copy(loading = false, provider = result.value, favorite = favorite) }
            }
        }
    }

    fun toggleFavorite() = viewModelScope.launch {
        val target = !state.value.favorite
        when (val result = favorites.setProviderFavorite(providerId, target)) {
            is ApiResult.Success -> state.update { it.copy(favorite = target) }
            is ApiResult.Failure -> state.update { it.copy(error = result.error) }
        }
    }
}
