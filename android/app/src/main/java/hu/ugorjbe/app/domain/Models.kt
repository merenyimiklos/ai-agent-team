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
    val startsWithinHours: Int? = null,
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
        NETWORK, SERVER, AUTH_REQUIRED, INVALID_CREDENTIALS, EMAIL_EXISTS,
        VALIDATION, NOT_FOUND, OFFER_NOT_BOOKABLE, INSUFFICIENT_CAPACITY,
        CANCELLATION_NOT_ALLOWED, UNKNOWN,
    }
}
