package hu.ugorjbe.app.ui.viewmodel

import hu.ugorjbe.app.MainDispatcherRule
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.testOfferSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun `load failure exposes retry then retry replaces it with content`() = runTest(dispatcherRule.dispatcher) {
        val repository = QueueCatalogRepository(
            ArrayDeque(
                listOf(
                    ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, retryable = true)),
                    ApiResult.Success(Page(listOf(testOfferSummary), 1, 20, 1, 1)),
                ),
            ),
        )
        val viewModel = DiscoveryViewModel(repository)
        assertTrue(viewModel.state.value.loading)
        runCurrent()
        assertEquals(ApiError.Kind.NETWORK, viewModel.state.value.error?.kind)

        viewModel.refresh()
        assertTrue(viewModel.state.value.loading)
        runCurrent()
        assertFalse(viewModel.state.value.loading)
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
    }
}

private class QueueCatalogRepository(
    private val results: ArrayDeque<ApiResult<Page<OfferSummary>>>,
) : CatalogRepository {
    override suspend fun offers(filter: OfferFilter) = results.removeFirst()
    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}
