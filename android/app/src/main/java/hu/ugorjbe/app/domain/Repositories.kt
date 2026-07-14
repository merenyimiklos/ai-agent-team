package hu.ugorjbe.app.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<Session>
    suspend fun register(email: String, password: String, displayName: String): ApiResult<Session>
    suspend fun logout()
}

interface CatalogRepository {
    suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>>
    suspend fun offer(id: String): ApiResult<OfferDetail>
    suspend fun provider(id: String): ApiResult<ProviderDetail>
}

interface BookingRepository {
    suspend fun create(offerId: String, quantity: Int): ApiResult<Booking>
    suspend fun list(scope: String): ApiResult<Page<Booking>>
    suspend fun detail(id: String): ApiResult<Booking>
    suspend fun cancel(id: String): ApiResult<Booking>
}

interface FavoritesRepository {
    suspend fun offers(): ApiResult<Page<OfferSummary>>
    suspend fun providers(): ApiResult<Page<ProviderSummary>>
    suspend fun setOfferFavorite(id: String, favorite: Boolean): ApiResult<Unit>
    suspend fun setProviderFavorite(id: String, favorite: Boolean): ApiResult<Unit>
}
