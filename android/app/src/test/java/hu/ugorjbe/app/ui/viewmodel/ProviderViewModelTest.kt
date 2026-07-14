package hu.ugorjbe.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import hu.ugorjbe.app.MainDispatcherRule
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.FavoritesRepository
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.Page
import hu.ugorjbe.app.domain.ProviderDetail
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.testProviderDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun `provider favorite toggles only after repository success`() = runTest(dispatcherRule.dispatcher) {
        val favorites = RecordingProviderFavorites()
        val viewModel = ProviderViewModel(
            SavedStateHandle(mapOf("providerId" to "provider")),
            ProviderCatalog(),
            favorites,
        )
        runCurrent()
        assertFalse(viewModel.state.value.favorite)

        viewModel.toggleFavorite()
        runCurrent()

        assertTrue(viewModel.state.value.favorite)
        assertTrue(favorites.lastFavorite)
    }
}

private class ProviderCatalog : CatalogRepository {
    override suspend fun offers(filter: OfferFilter): ApiResult<Page<OfferSummary>> = error("unused")
    override suspend fun offer(id: String): ApiResult<OfferDetail> = error("unused")
    override suspend fun provider(id: String): ApiResult<ProviderDetail> = ApiResult.Success(testProviderDetail)
}

private class RecordingProviderFavorites : FavoritesRepository {
    var lastFavorite = false
    override suspend fun offers() = ApiResult.Success(Page<OfferSummary>(emptyList(), 1, 50, 0, 0))
    override suspend fun providers() = ApiResult.Success(Page<ProviderSummary>(emptyList(), 1, 50, 0, 0))
    override suspend fun setOfferFavorite(id: String, favorite: Boolean) = ApiResult.Success(Unit)
    override suspend fun setProviderFavorite(id: String, favorite: Boolean): ApiResult<Unit> {
        lastFavorite = favorite
        return ApiResult.Success(Unit)
    }
}
