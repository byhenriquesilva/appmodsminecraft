package com.byhenriquesilva.atlasdemods.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Constrói um [AtlasApi] pra uma base URL. Não é cacheado como singleton
 * porque a URL pode mudar na tela de Ajustes — o custo de recriar o
 * Retrofit a cada chamada é irrelevante pra um app de uso pessoal.
 */
object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // downloads/commits no servidor podem demorar
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun create(baseUrl: String): AtlasApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AtlasApi::class.java)
    }
}
