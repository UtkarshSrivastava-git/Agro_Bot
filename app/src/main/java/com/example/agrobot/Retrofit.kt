package com.example.agrobot

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val ok = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply{
    level = HttpLoggingInterceptor.Level.BODY }).build()

val retrofit = Retrofit.Builder().baseUrl("https://YOUR_BACKEND_BASE_URL/")
    .client(ok)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val chatApi = retrofit.create(ChatApi::class.java)