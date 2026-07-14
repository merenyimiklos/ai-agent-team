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
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.domain.Session
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

data class DiscoveryUiState(
    val loading: Boolean = true,
    val offers: List<OfferSummary> = emptyList(),
    val filter: OfferFilter = OfferFilter(),
    val error: ApiError? = null,
)

@HiltViewModel
class DiscoveryViewModel @Inject constructor(private val repository: CatalogRepository) : ViewModel() {
    val state = MutableStateFlow(DiscoveryUiState())

    init { refresh() }

    fun setQuery(value: String) = state.update { it.copy(filter = it.filter.copy(query = value)) }
    fun applyFilter(filter: OfferFilter) {
        state.update { it.copy(filter = filter) }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        state.update { it.copy(loading = true, error = null) }
        when (val result = repository.offers(state.value.filter)) {
            is ApiResult.Success -> state.update {
                it.copy(loading = false, offers = result.value.items, error = null)
            }
            is ApiResult.Failure -> state.update { it.copy(loading = false, error = result.error) }
        }
    }
}

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

    init { refresh() }

    fun setScope(scope: String) {
        state.update { it.copy(scope = scope) }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        state.update { it.copy(loading = true, cancellingId = null, error = null) }
        when (val result = repository.list(state.value.scope)) {
            is ApiResult.Success -> state.update {
                it.copy(loading = false, bookings = result.value.items, error = null)
            }
            is ApiResult.Failure -> state.update { it.copy(loading = false, error = result.error) }
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
