package hu.ugorjbe.app.data

import hu.ugorjbe.app.data.remote.ApiCallRunner
import hu.ugorjbe.app.data.remote.CreateBookingRequest
import hu.ugorjbe.app.data.remote.LoginRequest
import hu.ugorjbe.app.data.remote.RegisterRequest
import hu.ugorjbe.app.data.remote.UgorjBeApi
import hu.ugorjbe.app.data.remote.toBookingPage
import hu.ugorjbe.app.data.remote.toOfferPage
import hu.ugorjbe.app.data.remote.toProviderPage
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.AuthRepository
import hu.ugorjbe.app.domain.BookingRepository
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.FavoritesRepository
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.Session
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UgorjBeApi,
    private val runner: ApiCallRunner,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override suspend fun login(email: String, password: String) = authenticate {
        api.login(LoginRequest(email.trim(), password))
    }

    override suspend fun register(email: String, password: String, displayName: String) = authenticate {
        api.register(RegisterRequest(email.trim(), password, displayName.trim()))
    }

    private suspend fun authenticate(block: suspend () -> hu.ugorjbe.app.data.remote.AuthResponseDto): ApiResult<Session> =
        when (val result = runner.call(block)) {
            is ApiResult.Success -> {
                val session = Session(result.value.accessToken, result.value.user.toDomain())
                sessionStore.save(session)
                ApiResult.Success(session)
            }
            is ApiResult.Failure -> result
        }

    override suspend fun logout() = sessionStore.clear()
}

class CatalogRepositoryImpl @Inject constructor(
    private val api: UgorjBeApi,
    private val runner: ApiCallRunner,
) : CatalogRepository {
    override suspend fun offers(filter: OfferFilter) = runner.call {
        val useLocation = filter.nearBudapestCenter
        val windowStart = filter.startsWithinHours?.let { Instant.now() }
        api.offers(
            query = filter.query.trim().ifBlank { null },
            categories = filter.category?.let(::listOf),
            childAge = filter.childAge,
            startsFromUtc = windowStart?.toString(),
            startsToUtc = windowStart?.plus(filter.startsWithinHours?.toLong() ?: 0, ChronoUnit.HOURS)?.toString(),
            maxPrice = filter.maxPrice,
            minAvailablePlaces = filter.minAvailablePlaces,
            latitude = if (useLocation) BigDecimal("47.4979") else null,
            longitude = if (useLocation) BigDecimal("19.0402") else null,
            maxDistanceKm = if (useLocation) BigDecimal.TEN else null,
            sort = if (filter.sort == "DISTANCE" && !useLocation) "START_TIME" else filter.sort,
            direction = filter.direction,
        ).toOfferPage()
    }

    override suspend fun mapOffers(bounds: MapBounds, filter: OfferFilter) = runner.call {
        val windowStart = filter.startsWithinHours?.let { Instant.now() }
        api.mapOffers(
            south = bounds.south,
            west = bounds.west,
            north = bounds.north,
            east = bounds.east,
            query = filter.query.trim().ifBlank { null },
            categories = filter.category?.let(::listOf),
            childAge = filter.childAge,
            startsFromUtc = windowStart?.toString(),
            startsToUtc = windowStart?.plus(filter.startsWithinHours?.toLong() ?: 0, ChronoUnit.HOURS)?.toString(),
            maxPrice = filter.maxPrice,
            minAvailablePlaces = filter.minAvailablePlaces,
            latitude = bounds.centerLatitude,
            longitude = bounds.centerLongitude,
            sort = filter.sort,
            direction = filter.direction,
        ).toDomain(bounds)
    }

    override suspend fun offer(id: String) = runner.call { api.offer(id).toDomain() }
    override suspend fun provider(id: String) = runner.call { api.provider(id).toDomain() }
}

class BookingRepositoryImpl @Inject constructor(
    private val api: UgorjBeApi,
    private val runner: ApiCallRunner,
) : BookingRepository {
    override suspend fun create(offerId: String, quantity: Int) =
        runner.call { api.createBooking(CreateBookingRequest(offerId, quantity)).toDomain() }

    override suspend fun list(scope: String) = runner.call { api.bookings(scope).toBookingPage() }
    override suspend fun detail(id: String) = runner.call { api.booking(id).toDomain() }
    override suspend fun cancel(id: String) = runner.call { api.cancelBooking(id).toDomain() }
}

class FavoritesRepositoryImpl @Inject constructor(
    private val api: UgorjBeApi,
    private val runner: ApiCallRunner,
) : FavoritesRepository {
    override suspend fun offers() = runner.call { api.favoriteOffers().toOfferPage() }
    override suspend fun providers() = runner.call { api.favoriteProviders().toProviderPage() }
    override suspend fun setOfferFavorite(id: String, favorite: Boolean) = runner.call {
        if (favorite) api.addFavoriteOffer(id) else api.removeFavoriteOffer(id)
    }
    override suspend fun setProviderFavorite(id: String, favorite: Boolean) = runner.call {
        if (favorite) api.addFavoriteProvider(id) else api.removeFavoriteProvider(id)
    }
}
