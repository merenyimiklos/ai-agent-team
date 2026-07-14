package hu.ugorjbe.app.data.remote

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.ApiError
import hu.ugorjbe.app.domain.ApiResult
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

class BigDecimalJsonAdapter : JsonAdapter<BigDecimal>() {
    override fun fromJson(reader: JsonReader): BigDecimal? =
        if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString().toBigDecimal()

    override fun toJson(writer: JsonWriter, value: BigDecimal?) {
        writer.value(value)
    }
}

@Singleton
class BearerInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionStore.currentToken() }
        val request = if (token == null) chain.request() else chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

object ProblemMapper {
    fun map(status: Int, problem: ProblemDto?): ApiError {
        val kind = when (problem?.code) {
            "AUTH_REQUIRED" -> ApiError.Kind.AUTH_REQUIRED
            "AUTH_INVALID_CREDENTIALS" -> ApiError.Kind.INVALID_CREDENTIALS
            "AUTH_EMAIL_EXISTS" -> ApiError.Kind.EMAIL_EXISTS
            "VALIDATION_FAILED" -> ApiError.Kind.VALIDATION
            "OFFER_NOT_FOUND", "PROVIDER_NOT_FOUND", "BOOKING_NOT_FOUND" -> ApiError.Kind.NOT_FOUND
            "OFFER_NOT_BOOKABLE" -> ApiError.Kind.OFFER_NOT_BOOKABLE
            "INSUFFICIENT_CAPACITY" -> ApiError.Kind.INSUFFICIENT_CAPACITY
            "CANCELLATION_NOT_ALLOWED" -> ApiError.Kind.CANCELLATION_NOT_ALLOWED
            "DEPENDENCY_UNAVAILABLE", "INTERNAL_ERROR" -> ApiError.Kind.SERVER
            else -> when (status) {
                401 -> ApiError.Kind.AUTH_REQUIRED
                404 -> ApiError.Kind.NOT_FOUND
                in 500..599 -> ApiError.Kind.SERVER
                else -> ApiError.Kind.UNKNOWN
            }
        }
        return ApiError(
            kind = kind,
            code = problem?.code,
            detail = problem?.detail,
            availablePlaces = problem?.availablePlaces,
            retryable = kind == ApiError.Kind.SERVER,
        )
    }
}

@Singleton
class ApiCallRunner @Inject constructor(
    moshi: Moshi,
    private val sessionStore: SessionStore,
) {
    private val problemAdapter = moshi.adapter(ProblemDto::class.java)

    suspend fun <T> call(block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (exception: HttpException) {
        val problem = runCatching {
            exception.response()?.errorBody()?.string()?.let(problemAdapter::fromJson)
        }.getOrNull()
        val mapped = ProblemMapper.map(exception.code(), problem)
        if (mapped.kind == ApiError.Kind.AUTH_REQUIRED) sessionStore.clear()
        ApiResult.Failure(mapped)
    } catch (_: IOException) {
        ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, retryable = true))
    } catch (_: Exception) {
        ApiResult.Failure(ApiError(ApiError.Kind.UNKNOWN))
    }
}
