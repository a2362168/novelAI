package com.example.noveljson

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


object HttpFunctions {
    private val httpClient : OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
        with(builder) {
            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(1, TimeUnit.MINUTES)
        }
        httpClient = builder.build()
    }

    fun createRetrofit(baseUrl: String): Retrofit {
        val jacksonConverterFactory = JacksonConverterFactory.create(JacksonFactory.objectMapper)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jacksonConverterFactory)
            .client(httpClient)
            .build()
    }
}