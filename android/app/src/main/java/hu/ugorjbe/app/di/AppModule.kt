package hu.ugorjbe.app.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.data.AuthRepositoryImpl
import hu.ugorjbe.app.data.BookingRepositoryImpl
import hu.ugorjbe.app.data.CatalogRepositoryImpl
import hu.ugorjbe.app.data.FavoritesRepositoryImpl
import hu.ugorjbe.app.data.remote.BearerInterceptor
import hu.ugorjbe.app.data.remote.BigDecimalJsonAdapter
import hu.ugorjbe.app.data.remote.UgorjBeApi
import hu.ugorjbe.app.data.session.DataStoreSessionStore
import hu.ugorjbe.app.data.session.SessionStore
import hu.ugorjbe.app.domain.AuthRepository
import hu.ugorjbe.app.domain.BookingRepository
import hu.ugorjbe.app.domain.CatalogRepository
import hu.ugorjbe.app.domain.FavoritesRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.math.BigDecimal
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds @Singleton abstract fun sessionStore(impl: DataStoreSessionStore): SessionStore
    @Binds abstract fun authRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds abstract fun catalogRepository(impl: CatalogRepositoryImpl): CatalogRepository
    @Binds abstract fun bookingRepository(impl: BookingRepositoryImpl): BookingRepository
    @Binds abstract fun favoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun moshi(): Moshi = Moshi.Builder()
        .add(BigDecimal::class.java, BigDecimalJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides @Singleton
    fun okHttp(interceptor: BearerInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton
    fun api(retrofit: Retrofit): UgorjBeApi = retrofit.create(UgorjBeApi::class.java)
}
