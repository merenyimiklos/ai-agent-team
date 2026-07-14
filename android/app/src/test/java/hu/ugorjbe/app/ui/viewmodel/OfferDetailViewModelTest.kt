package hu.ugorjbe.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import hu.ugorjbe.app.MainDispatcherRule
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.BookingRepository
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.FavoritesRepository
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.testBooking
import hu.ugorjbe.app.testOfferDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfferDetailViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun `browse detail reserve flow renders only authoritative booking response`() = runTest(dispatcherRule.dispatcher) {
        val bookings = FakeBookingRepository(ApiResult.Success(testBooking))
        val viewModel = OfferDetailViewModel(
            SavedStateHandle(mapOf("offerId" to "offer")),
            FakeCatalog(), bookings, FakeFavorites(),
        )
        runCurrent()
        assertEquals(testOfferDetail, viewModel.state.value.offer)

        viewModel.setQuantity(2)
        viewModel.reserve()
        assertTrue(viewModel.state.value.reserving)
        runCurrent()

        assertFalse(viewModel.state.value.reserving)
        assertEquals(testBooking, viewModel.state.value.booking)
        assertEquals(2, bookings.lastQuantity)
        assertEquals("ugorjbe://booking/booking?code=UGB-7K3M9Q", viewModel.state.value.booking?.qrPayload)
    }

    @Test fun `capacity conflict applies server limit without reporting success`() = runTest(dispatcherRule.dispatcher) {
        val bookings = FakeBookingRepository(
            ApiResult.Failure(ApiError(ApiError.Kind.INSUFFICIENT_CAPACITY, availablePlaces = 1)),
        )
        val viewModel = OfferDetailViewModel(
            SavedStateHandle(mapOf("offerId" to "offer")),
            FakeCatalog(), bookings, FakeFavorites(),
        )
        runCurrent()
        viewModel.setQuantity(3)
        viewModel.reserve()
        runCurrent()
        assertEquals(1, viewModel.state.value.quantity)
        assertEquals(1, viewModel.state.value.offer?.availablePlaces)
        assertEquals(null, viewModel.state.value.booking)
        assertEquals(ApiError.Kind.INSUFFICIENT_CAPACITY, viewModel.state.value.message?.kind)
    }
}

private class FakeCatalog : CatalogRepository {
    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> = error("unused")
    override suspend fun offer(id: String) = ApiResult.Success(testOfferDetail)
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}

private class FakeBookingRepository(private val createResult: ApiResult<Booking>) : BookingRepository {
    var lastQuantity: Int? = null
    override suspend fun create(offerId: String, quantity: Int): ApiResult<Booking> {
        lastQuantity = quantity
        return createResult
    }
    override suspend fun list(scope: String): ApiResult<Page<Booking>> = error("unused")
    override suspend fun detail(id: String): ApiResult<Booking> = error("unused")
    override suspend fun cancel(id: String): ApiResult<Booking> = error("unused")
}

private class FakeFavorites : FavoritesRepository {
    override suspend fun offers() = ApiResult.Success(Page<OfferSummary>(emptyList(), 1, 50, 0, 0))
    override suspend fun providers() = ApiResult.Success(Page<ProviderSummary>(emptyList(), 1, 50, 0, 0))
    override suspend fun setOfferFavorite(id: String, favorite: Boolean) = ApiResult.Success(Unit)
    override suspend fun setProviderFavorite(id: String, favorite: Boolean) = ApiResult.Success(Unit)
}
