package com.example.noveljson

import okhttp3.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object JacksonFactory {
    val objectMapper by lazy {
        ObjectMapper().apply {
            registerModule(KotlinModule(nullIsSameAsDefault = true, nullToEmptyCollection = true))
            registerModule(JavaTimeModule())
            enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
            enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
        }
    }
}