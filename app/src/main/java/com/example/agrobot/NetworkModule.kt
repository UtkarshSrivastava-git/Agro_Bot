package com.example.agrobot

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
        fun create(baseUrl: String) : ChatApi {
            val logging  = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            //val client = OkHttpClient.Builder().addInterceptor(logging).build()
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder().baseUrl(baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ChatApi::class.java)
        }
    }
