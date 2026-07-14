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
import kotlinx.coroutines.CompletableDeferred
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
        val retryResult = CompletableDeferred<ApiResult<Page<OfferSummary>>>()
        val repository = RetryCatalogRepository(retryResult)
        val viewModel = DiscoveryViewModel(repository)
        assertTrue(viewModel.state.value.loading)
        runCurrent()
        assertEquals(ApiError.Kind.NETWORK, viewModel.state.value.error?.kind)
        assertEquals(24, repository.filters.single().startsWithinHours)

        viewModel.refresh()
        runCurrent()
        assertTrue(viewModel.state.value.loading)
        retryResult.complete(ApiResult.Success(Page(listOf(testOfferSummary), 1, 20, 1, 1)))
        runCurrent()
        assertFalse(viewModel.state.value.loading)
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
    }

    @Test fun `later filter wins over an obsolete request`() = runTest(dispatcherRule.dispatcher) {
        val firstResult = CompletableDeferred<ApiResult<Page<OfferSummary>>>()
        val repository = FilterRaceCatalogRepository(firstResult)
        val viewModel = DiscoveryViewModel(repository)
        runCurrent()

        val requestedFilter = OfferFilter(query = "uszoda")
        viewModel.applyFilter(requestedFilter)
        runCurrent()

        assertEquals(requestedFilter, viewModel.state.value.filter)
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)

        firstResult.complete(ApiResult.Success(Page(emptyList(), 1, 20, 0, 0)))
        runCurrent()
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
    }
}

private class RetryCatalogRepository(
    private val retryResult: CompletableDeferred<ApiResult<Page<OfferSummary>>>,
) : CatalogRepository {
    private var calls = 0
    val filters = mutableListOf<OfferFilter>()
    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> {
        filters += filter
        return if (calls++ == 0) ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, retryable = true))
        else retryResult.await()
    }
    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}

private class FilterRaceCatalogRepository(
    private val firstResult: CompletableDeferred<ApiResult<Page<OfferSummary>>>,
) : CatalogRepository {
    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> =
        if (filter.query.isBlank()) firstResult.await()
        else ApiResult.Success(Page(listOf(testOfferSummary), 1, 20, 1, 1))

    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}
