package hu.ugorjbe.app.domain

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String)

data class Address(
    val postalCode: String,
    val city: String,
    val street: String,
    val countryCode: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val locale: String,
    val createdAtUtc: String,
    val role: String = "customer",
)

data class ProviderSummary(
    val id: String,
    val name: String,
    val shortDescription: String,
    val address: Address,
    val imageUrl: String?,
)

data class ProviderDetail(
    val id: String,
    val name: String,
    val shortDescription: String,
    val description: String,
    val address: Address,
    val phone: String?,
    val email: String?,
    val websiteUrl: String?,
    val accessibilityInfo: String?,
    val imageUrl: String?,
    val activeOfferCount: Int,
)

data class OfferSummary(
    val id: String,
    val provider: ProviderSummary,
    val title: String,
    val category: String,
    val startsAtUtc: String,
    val endsAtUtc: String,
    val minChildAge: Int,
    val maxChildAge: Int,
    val originalUnitPrice: Money,
    val discountedUnitPrice: Money,
    val discountPercent: Int,
    val availablePlaces: Int,
    val distanceKm: BigDecimal?,
    val imageUrl: String?,
    val address: Address = provider.address,
)

data class OfferDetail(
    val id: String,
    val provider: ProviderSummary,
    val title: String,
    val description: String,
    val category: String,
    val startsAtUtc: String,
    val endsAtUtc: String,
    val bookingCutoffUtc: String,
    val cancelUntilUtc: String,
    val minChildAge: Int,
    val maxChildAge: Int,
    val accompanimentRequired: Boolean,
    val accessibilityInfo: String?,
    val originalUnitPrice: Money,
    val discountedUnitPrice: Money,
    val discountPercent: Int,
    val totalCapacity: Int,
    val availablePlaces: Int,
    val isBookable: Boolean,
    val unavailableReason: String?,
    val paymentMethod: String,
    val distanceKm: BigDecimal?,
    val imageUrl: String?,
    val address: Address = provider.address,
)

data class MapBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    val latitudeSpan: Double get() = north - south
    val longitudeSpan: Double get() = east - west
    val centerLatitude: Double get() = (south + north) / 2.0
    val centerLongitude: Double get() = (west + east) / 2.0

    fun isApiValid(): Boolean = south in -90.0..90.0 && north in -90.0..90.0 &&
        west in -180.0..180.0 && east in -180.0..180.0 && south < north && west < east &&
        latitudeSpan <= 2.0 && longitudeSpan <= 3.0

    companion object {
        val Budapest = MapBounds(47.4200, 18.9200, 47.5900, 19.1800)
    }
}

data class MapViewport(val bounds: MapBounds, val zoom: Float)

data class MapOfferEnvelope(
    val items: List<OfferSummary>,
    val returnedCount: Int,
    val limit: Int,
    val isTruncated: Boolean,
    val bounds: MapBounds,
)

data class BookingOffer(
    val id: String,
    val title: String,
    val category: String,
    val providerId: String,
    val providerName: String,
    val startsAtUtc: String,
    val endsAtUtc: String,
    val address: Address,
    val imageUrl: String?,
)

data class Booking(
    val id: String,
    val status: String,
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money,
    val paymentMethod: String,
    val bookingCode: String,
    val qrPayload: String,
    val createdAtUtc: String,
    val cancelledAtUtc: String?,
    val cancellationAllowed: Boolean,
    val cancellationDeadlineUtc: String,
    val offer: BookingOffer,
)

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int,
)

data class OfferFilter(
    val query: String = "",
    val category: String? = null,
    val childAge: Int? = null,
    val startsWithinHours: Int? = 24,
    val maxPrice: BigDecimal? = null,
    val minAvailablePlaces: Int = 1,
    val nearBudapestCenter: Boolean = false,
    val sort: String = "START_TIME",
    val direction: String = "ASC",
)

data class Session(val accessToken: String, val user: User)

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

data class ApiError(
    val kind: Kind,
    val code: String? = null,
    val detail: String? = null,
    val availablePlaces: Int? = null,
    val retryable: Boolean = false,
) {
    enum class Kind {
        NETWORK,
        CONTRACT,
        SERVER,
        AUTH_REQUIRED,
        INVALID_CREDENTIALS,
        EMAIL_EXISTS,
        VALIDATION,
        NOT_FOUND,
        OFFER_NOT_BOOKABLE,
        INSUFFICIENT_CAPACITY,
        CANCELLATION_NOT_ALLOWED,
        UNKNOWN,
    }
}
