package ru.touchemiasapp.data.api.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.touchemiasapp.BuildConfig
import ru.touchemiasapp.data.api.EmiasApi
import ru.touchemiasapp.data.api.auth.AuthApi
import ru.touchemiasapp.data.auth.AuthRepository
import ru.touchemiasapp.data.auth.SudirAuthDataStore
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        @Named("plain") base: OkHttpClient,
        authDataStore: SudirAuthDataStore,
        // Use Provider to break circular dependency: AuthRepository → AuthApi → plain client
        authRepositoryProvider: dagger.Lazy<AuthRepository>
    ): OkHttpClient = base.newBuilder()
        .addInterceptor { chain ->
            val token = authDataStore.getAccessTokenSync()
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .header("EI-Token", token)
                    .build()
            }
            chain.proceed(request)
        }
        .authenticator(object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                // Prevent infinite retry loop
                if (response.request.header("EI-Token-Retried") != null) return null
                val refreshed = kotlinx.coroutines.runBlocking {
                    authRepositoryProvider.get().refreshToken()
                }
                if (!refreshed) return null
                val newToken = authDataStore.getAccessTokenSync() ?: return null
                return response.request.newBuilder()
                    .header("EI-Token", newToken)
                    .header("EI-Token-Retried", "true")
                    .build()
            }
        })
        .build()

    @Provides
    @Singleton
    @Named("eip")
    fun provideEipRetrofit(@Named("auth") client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://emias.info/api-eip/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("plain_retrofit")
    fun providePlainRetrofit(@Named("plain") client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://emias.info/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideEmiasApi(@Named("eip") retrofit: Retrofit): EmiasApi =
        retrofit.create(EmiasApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(@Named("plain_retrofit") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}
