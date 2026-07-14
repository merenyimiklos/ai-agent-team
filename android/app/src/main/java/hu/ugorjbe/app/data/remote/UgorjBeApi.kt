package hu.ugorjbe.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface UgorjBeApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseDto

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @GET("api/offers")
    suspend fun offers(
        @Query("q") query: String? = null,
        @Query("providerId") providerId: String? = null,
        @Query("category") categories: List<String>? = null,
        @Query("childAge") childAge: Int? = null,
        @Query("minAvailablePlaces") minAvailablePlaces: Int = 1,
        @Query("latitude") latitude: BigDecimal? = null,
        @Query("longitude") longitude: BigDecimal? = null,
        @Query("maxDistanceKm") maxDistanceKm: BigDecimal? = null,
        @Query("sort") sort: String = "START_TIME",
        @Query("direction") direction: String = "ASC",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): PageDto<OfferSummaryDto>

    @GET("api/offers/{offerId}")
    suspend fun offer(@Path("offerId") offerId: String): OfferDetailDto

    @GET("api/providers/{providerId}")
    suspend fun provider(@Path("providerId") providerId: String): ProviderDetailDto

    @POST("api/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): BookingDto

    @GET("api/bookings")
    suspend fun bookings(
        @Query("scope") scope: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): PageDto<BookingDto>

    @GET("api/bookings/{bookingId}")
    suspend fun booking(@Path("bookingId") bookingId: String): BookingDto

    @POST("api/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String): BookingDto

    @GET("api/favorites/offers")
    suspend fun favoriteOffers(@Query("pageSize") pageSize: Int = 50): PageDto<OfferSummaryDto>

    @PUT("api/favorites/offers/{offerId}")
    suspend fun addFavoriteOffer(@Path("offerId") offerId: String)

    @DELETE("api/favorites/offers/{offerId}")
    suspend fun removeFavoriteOffer(@Path("offerId") offerId: String)

    @GET("api/favorites/providers")
    suspend fun favoriteProviders(@Query("pageSize") pageSize: Int = 50): PageDto<ProviderSummaryDto>

    @PUT("api/favorites/providers/{providerId}")
    suspend fun addFavoriteProvider(@Path("providerId") providerId: String)

    @DELETE("api/favorites/providers/{providerId}")
    suspend fun removeFavoriteProvider(@Path("providerId") providerId: String)
}
