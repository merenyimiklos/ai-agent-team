package hu.ugorjbe.app.data.remote

import hu.ugorjbe.app.domain.ApiError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemMapperTest {
    @Test fun `capacity problem retains authoritative availability`() {
        val result = ProblemMapper.map(
            409,
            ProblemDto(status = 409, code = "INSUFFICIENT_CAPACITY", availablePlaces = 2),
        )
        assertEquals(ApiError.Kind.INSUFFICIENT_CAPACITY, result.kind)
        assertEquals(2, result.availablePlaces)
        assertFalse(result.retryable)
    }

    @Test fun `service dependency errors are retryable`() {
        val result = ProblemMapper.map(503, ProblemDto(code = "DEPENDENCY_UNAVAILABLE"))
        assertEquals(ApiError.Kind.SERVER, result.kind)
        assertTrue(result.retryable)
    }

    @Test fun `login credentials remain an inline auth error`() {
        val result = ProblemMapper.map(401, ProblemDto(code = "AUTH_INVALID_CREDENTIALS"))
        assertEquals(ApiError.Kind.INVALID_CREDENTIALS, result.kind)
    }
}
