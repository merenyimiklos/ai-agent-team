package hu.ugorjbe.app.ui.viewmodel

import hu.ugorjbe.app.MainDispatcherRule
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.BookingRepository
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.testBooking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingsViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun `later booking scope wins over an obsolete request`() = runTest(dispatcherRule.dispatcher) {
        val active = CompletableDeferred<ApiResult<Page<Booking>>>()
        val repository = DeferredBookingRepository(active)
        val viewModel = BookingsViewModel(repository)
        runCurrent()

        viewModel.setScope("PREVIOUS")
        runCurrent()

        assertEquals("PREVIOUS", viewModel.state.value.scope)
        assertFalse(viewModel.state.value.loading)
        assertEquals(listOf(testBooking), viewModel.state.value.bookings)
        assertEquals(listOf("ACTIVE", "PREVIOUS"), repository.requestedScopes)

        active.complete(ApiResult.Success(Page(emptyList(), 1, 20, 0, 0)))
        runCurrent()
        assertEquals(listOf(testBooking), viewModel.state.value.bookings)
    }
}

private class DeferredBookingRepository(
    private val active: CompletableDeferred<ApiResult<Page<Booking>>>,
) : BookingRepository {
    val requestedScopes = mutableListOf<String>()

    override suspend fun list(scope: String): ApiResult<Page<Booking>> {
        requestedScopes += scope
        return if (scope == "ACTIVE") active.await()
        else ApiResult.Success(Page(listOf(testBooking), 1, 20, 1, 1))
    }

    override suspend fun create(offerId: String, quantity: Int): ApiResult<Booking> = error("unused")
    override suspend fun detail(id: String): ApiResult<Booking> = error("unused")
    override suspend fun cancel(id: String): ApiResult<Booking> = error("unused")
}
