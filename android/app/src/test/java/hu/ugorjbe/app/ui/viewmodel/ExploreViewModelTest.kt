package hu.ugorjbe.app.ui.viewmodel

import hu.ugorjbe.app.MainDispatcherRule
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.MapBounds
import hu.ugorjbe.app.domain.MapOfferEnvelope
import hu.ugorjbe.app.domain.MapViewport
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.testOfferSummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun `map and list share results filters and selection without refetch`() = runTest(dispatcherRule.dispatcher) {
        val repository = ImmediateMapRepository()
        val viewModel = ExploreViewModel(repository)
        runCurrent()

        assertEquals(ExplorePresentation.MAP, viewModel.state.value.presentation)
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
        assertEquals(1, repository.calls)

        viewModel.selectOffer(testOfferSummary.id)
        viewModel.setPresentation(ExplorePresentation.LIST)

        assertEquals(ExplorePresentation.LIST, viewModel.state.value.presentation)
        assertEquals(testOfferSummary, viewModel.state.value.selectedOffer)
        assertEquals(OfferFilter(), viewModel.state.value.filter)
        assertEquals(1, repository.calls)
    }

    @Test fun `selection rejects an id outside the loaded envelope`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = ExploreViewModel(ImmediateMapRepository())
        runCurrent()

        viewModel.selectOffer("missing")

        assertNull(viewModel.state.value.selectedOfferId)
    }

    @Test fun `camera threshold exposes area search only after meaningful movement`() = runTest(dispatcherRule.dispatcher) {
        val repository = ImmediateMapRepository()
        val viewModel = ExploreViewModel(repository)
        runCurrent()
        val baseline = viewModel.state.value.searchedViewport

        viewModel.onCameraIdle(baseline.copy(zoom = 12.2f))
        assertFalse(viewModel.state.value.searchThisAreaVisible)

        viewModel.onCameraIdle(baseline.copy(zoom = 12.6f))
        assertTrue(viewModel.state.value.searchThisAreaVisible)
    }

    @Test fun `oversized viewport asks for zoom and never calls API`() = runTest(dispatcherRule.dispatcher) {
        val repository = ImmediateMapRepository()
        val viewModel = ExploreViewModel(repository)
        runCurrent()

        viewModel.onCameraIdle(viewModel.state.value.searchedViewport)
        viewModel.onCameraIdle(MapViewport(MapBounds(40.0, 10.0, 43.0, 14.0), 5f))
        viewModel.searchThisArea()
        runCurrent()

        assertTrue(viewModel.state.value.areaTooLarge)
        assertEquals(1, repository.calls)
    }

    @Test fun `permission denial preserves Budapest discovery`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = ExploreViewModel(ImmediateMapRepository())
        runCurrent()

        viewModel.requestLocation()
        assertEquals(LocationPermissionState.RATIONALE, viewModel.state.value.locationPermission)
        viewModel.onLocationPermissionResult(granted = false)

        assertEquals(LocationPermissionState.DENIED, viewModel.state.value.locationPermission)
        assertEquals(MapBounds.Budapest, viewModel.state.value.searchedViewport.bounds)
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
    }

    @Test fun `only newest request generation may replace results`() = runTest(dispatcherRule.dispatcher) {
        val first = CompletableDeferred<ApiResult<MapOfferEnvelope>>()
        val repository = RacingMapRepository(first)
        val viewModel = ExploreViewModel(repository)
        runCurrent()

        viewModel.applyFilter(OfferFilter(query = "agyag"))
        runCurrent()
        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)

        first.complete(envelope(emptyList()))
        runCurrent()

        assertEquals(listOf(testOfferSummary), viewModel.state.value.offers)
        assertEquals("agyag", viewModel.state.value.filter.query)
    }

    @Test fun `bbox policy follows frozen API limits and IoU`() {
        assertTrue(MapBounds.Budapest.isApiValid())
        assertFalse(MapBounds(-10.0, -10.0, 10.0, 10.0).isApiValid())
        assertEquals(1.0, MapSearchPolicy.intersectionOverUnion(MapBounds.Budapest, MapBounds.Budapest), 0.0001)
        val moved = MapViewport(MapBounds(47.42, 19.04, 47.59, 19.30), 12f)
        assertTrue(MapSearchPolicy.shouldOfferSearch(MapViewport(MapBounds.Budapest, 12f), moved))
    }
}

private fun envelope(items: List<OfferSummary>) = ApiResult.Success(
    MapOfferEnvelope(items, items.size, 200, false, MapBounds.Budapest),
)

private class ImmediateMapRepository : CatalogRepository {
    var calls = 0
    override suspend fun mapOffers(bounds: MapBounds, filter: OfferFilter): ApiResult<MapOfferEnvelope> {
        calls++
        return envelope(listOf(testOfferSummary))
    }
    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> = error("unused")
    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}

private class RacingMapRepository(
    private val first: CompletableDeferred<ApiResult<MapOfferEnvelope>>,
) : CatalogRepository {
    override suspend fun mapOffers(bounds: MapBounds, filter: OfferFilter): ApiResult<MapOfferEnvelope> =
        if (filter.query.isBlank()) withContext(NonCancellable) { first.await() }
        else envelope(listOf(testOfferSummary))

    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> = error("unused")
    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = error("unused")
}
