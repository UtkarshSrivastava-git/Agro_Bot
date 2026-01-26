package com.example.agrobot

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

    object NetworkModule {
        fun create(baseUrl: String) : ChatApi {
            val logging  = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder().addInterceptor(logging).build()
            val retrofit = Retrofit.Builder().baseUrl(baseUrl).client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(ChatApi::class.java)
        }
    }
