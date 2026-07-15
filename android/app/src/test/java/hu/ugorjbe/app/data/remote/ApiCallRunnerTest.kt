package hu.ugorjbe.app.data.remote

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.ApiResult
import hu.ugorjbe.app.domain.Session
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ApiCallRunnerTest {
    private fun runner() = ApiCallRunner(
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
        EmptySessionStore,
    )

    @Test
    fun `coroutine cancellation is rethrown`() = runTest {
        try {
            runner().call<Unit> { throw CancellationException("obsolete request") }
            fail("CancellationException should escape ApiCallRunner")
        } catch (_: CancellationException) {
            // Expected: cancellation must retain structured-concurrency semantics.
        }
    }

    @Test
    fun `malformed response is reported as contract failure`() = runTest {
        val result = runner().call<Unit> { throw JsonDataException("Required value missing at $.items") }

        assertTrue(result is ApiResult.Failure)
        val error = (result as ApiResult.Failure).error
        assertEquals(ApiError.Kind.CONTRACT, error.kind)
        assertEquals("API_CONTRACT_INVALID", error.code)
        assertFalse(error.retryable)
    }

    @Test
    fun `transport IOException remains a retryable network failure`() = runTest {
        val result = runner().call<Unit> { throw IOException("connection refused") }

        assertTrue(result is ApiResult.Failure)
        val error = (result as ApiResult.Failure).error
        assertEquals(ApiError.Kind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }
}

private object EmptySessionStore : SessionStore {
    override val session: Flow<Session?> = flowOf(null)
    override suspend fun currentToken(): String? = null
    override suspend fun save(session: Session) = Unit
    override suspend fun clear() = Unit
}
