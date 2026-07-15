package hu.ugorjbe.app.data.remote

import com.squareup.moshi.JsonClass
import hu.ugorjbe.app.domain.Address
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.BookingOffer
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.MapOfferEnvelope
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.domain.User
import java.math.BigDecimal

@JsonClass(generateAdapter = false)
data class MoneyDto(val amount: BigDecimal, val currency: String) {
    fun toDomain() = Money(amount, currency)
}

@JsonClass(generateAdapter = false)
data class AddressDto(
    val postalCode: String,
    val city: String,
    val street: String,
    val countryCode: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
) {
    fun toDomain() = Address(postalCode, city, street, countryCode, latitude, longitude)
}

@JsonClass(generateAdapter = false)
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val locale: String,
    val createdAtUtc: String,
    val role: String = "customer",
) {
    fun toDomain() = User(id, email, displayName, locale, createdAtUtc, role)
}

@JsonClass(generateAdapter = false)
data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresAtUtc: String,
    val user: UserDto,
)

@JsonClass(generateAdapter = false)
data class ProviderSummaryDto(
    val id: String,
    val name: String,
    val shortDescription: String,
    val address: AddressDto,
    val imageUrl: String?,
) {
    fun toDomain() = ProviderSummary(id, name, shortDescription, address.toDomain(), imageUrl)
}

@JsonClass(generateAdapter = false)
data class ProviderDetailDto(
    val id: String,
    val name: String,
    val shortDescription: String,
    val description: String,
    val address: AddressDto,
    val phone: String?,
    val email: String?,
    val websiteUrl: String?,
    val accessibilityInfo: String?,
    val imageUrl: String?,
    val activeOfferCount: Int,
) {
    fun toDomain() = ProviderDetail(
        id, name, shortDescription, description, address.toDomain(), phone, email,
        websiteUrl, accessibilityInfo, imageUrl, activeOfferCount,
    )
}

@JsonClass(generateAdapter = false)
data class OfferSummaryDto(
    val id: String,
    val provider: ProviderSummaryDto,
    val title: String,
    val category: String,
    val startsAtUtc: String,
    val endsAtUtc: String,
    val minChildAge: Int,
    val maxChildAge: Int,
    val originalUnitPrice: MoneyDto,
    val discountedUnitPrice: MoneyDto,
    val discountPercent: Int,
    val availablePlaces: Int,
    val distanceKm: BigDecimal?,
    val imageUrl: String?,
    val address: AddressDto,
) {
    fun toDomain() = OfferSummary(
        id, provider.toDomain(), title, category, startsAtUtc, endsAtUtc, minChildAge,
        maxChildAge, originalUnitPrice.toDomain(), discountedUnitPrice.toDomain(),
        discountPercent, availablePlaces, distanceKm, imageUrl, address.toDomain(),
    )
}

@JsonClass(generateAdapter = false)
data class OfferDetailDto(
    val id: String,
    val provider: ProviderSummaryDto,
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
    val originalUnitPrice: MoneyDto,
    val discountedUnitPrice: MoneyDto,
    val discountPercent: Int,
    val totalCapacity: Int,
    val availablePlaces: Int,
    val isBookable: Boolean,
    val unavailableReason: String?,
    val paymentMethod: String,
    val distanceKm: BigDecimal?,
    val imageUrl: String?,
    val address: AddressDto,
) {
    fun toDomain() = OfferDetail(
        id, provider.toDomain(), title, description, category, startsAtUtc, endsAtUtc,
        bookingCutoffUtc, cancelUntilUtc, minChildAge, maxChildAge, accompanimentRequired,
        accessibilityInfo, originalUnitPrice.toDomain(), discountedUnitPrice.toDomain(),
        discountPercent, totalCapacity, availablePlaces, isBookable, unavailableReason,
        paymentMethod, distanceKm, imageUrl, address.toDomain(),
    )
}

@JsonClass(generateAdapter = false)
data class MapOfferEnvelopeDto(
    val items: List<OfferSummaryDto>,
    val isTruncated: Boolean,
    val limit: Int,
) {
    fun toDomain(bounds: MapBounds) = MapOfferEnvelope(
        items = items.map { it.toDomain() },
        returnedCount = items.size,
        limit = limit,
        isTruncated = isTruncated,
        bounds = bounds,
    )
}

@JsonClass(generateAdapter = false)
data class BookingOfferDto(
    val id: String,
    val title: String,
    val category: String,
    val providerId: String,
    val providerName: String,
    val startsAtUtc: String,
    val endsAtUtc: String,
    val address: AddressDto,
    val imageUrl: String?,
) {
    fun toDomain() = BookingOffer(
        id, title, category, providerId, providerName, startsAtUtc, endsAtUtc,
        address.toDomain(), imageUrl,
    )
}

@JsonClass(generateAdapter = false)
data class BookingDto(
    val id: String,
    val status: String,
    val quantity: Int,
    val unitPrice: MoneyDto,
    val totalPrice: MoneyDto,
    val paymentMethod: String,
    val bookingCode: String,
    val qrPayload: String,
    val createdAtUtc: String,
    val cancelledAtUtc: String?,
    val cancellationAllowed: Boolean,
    val cancellationDeadlineUtc: String,
    val offer: BookingOfferDto,
) {
    fun toDomain() = Booking(
        id, status, quantity, unitPrice.toDomain(), totalPrice.toDomain(), paymentMethod,
        bookingCode, qrPayload, createdAtUtc, cancelledAtUtc, cancellationAllowed,
        cancellationDeadlineUtc, offer.toDomain(),
    )
}

@JsonClass(generateAdapter = false)
data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int,
)

fun PageDto<OfferSummaryDto>.toOfferPage() = Page(items.map { it.toDomain() }, page, pageSize, totalCount, totalPages)
fun PageDto<ProviderSummaryDto>.toProviderPage() = Page(items.map { it.toDomain() }, page, pageSize, totalCount, totalPages)
fun PageDto<BookingDto>.toBookingPage() = Page(items.map { it.toDomain() }, page, pageSize, totalCount, totalPages)

@JsonClass(generateAdapter = false)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = false)
data class RegisterRequest(val email: String, val password: String, val displayName: String)

@JsonClass(generateAdapter = false)
data class CreateBookingRequest(val offerId: String, val quantity: Int)

@JsonClass(generateAdapter = false)
data class ProblemDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val code: String? = null,
    val traceId: String? = null,
    val availablePlaces: Int? = null,
    val errors: Map<String, List<String>>? = null,
)
