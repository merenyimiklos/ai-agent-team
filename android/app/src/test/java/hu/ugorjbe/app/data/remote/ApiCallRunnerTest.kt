package hu.ugorjbe.app.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.Session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test

class ApiCallRunnerTest {
    @Test fun `coroutine cancellation is rethrown`() = runTest {
        val runner = ApiCallRunner(
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            EmptySessionStore,
        )

        try {
            runner.call<Unit> { throw CancellationException("obsolete request") }
            fail("CancellationException should escape ApiCallRunner")
        } catch (exception: CancellationException) {
            // Expected: cancellation must retain structured-concurrency semantics.
        }
    }
}

private object EmptySessionStore : SessionStore {
    override val session: Flow<Session?> = flowOf(null)
    override suspend fun currentToken(): String? = null
    override suspend fun save(session: Session) = Unit
    override suspend fun clear() = Unit
}
