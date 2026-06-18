package ru.touchemiasapp.data.api.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.touchemiasapp.BuildConfig
import ru.touchemiasapp.data.api.EmiasApi
import ru.touchemiasapp.data.api.auth.AuthApi
import ru.touchemiasapp.data.auth.SudirAuthDataStore
import java.util.concurrent.ConcurrentHashMap
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
    fun provideCookieJar(): CookieJar = object : CookieJar {
        private val store = ConcurrentHashMap<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> =
            store[url.host] ?: emptyList()
    }

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(cookieJar: CookieJar): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        @Named("plain") base: OkHttpClient,
        authDataStore: SudirAuthDataStore
    ): OkHttpClient = base.newBuilder()
        .addInterceptor { chain ->
            val cookies = authDataStore.getSessionCookiesSync()
            val eiToken = authDataStore.getEiTokenSync()
            val builder = chain.request().newBuilder()
            if (!cookies.isNullOrBlank()) builder.header("Cookie", cookies)
            if (!eiToken.isNullOrBlank()) {
                builder.header("EI-Token", eiToken)
                builder.header("X-App", "portal")
            }
            chain.proceed(builder.build())
        }
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
