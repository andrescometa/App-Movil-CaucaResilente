package com.example.cauca_resilente_app.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // IP LAN de este PC (sirve en emulador y dispositivo físico en la misma red).
    // Alternativas:
    // - Emulador con adb reverse tcp:3000 tcp:3000 -> "http://127.0.0.1:3000/"
    // - Emulador -> "http://10.0.2.2:3000/" (en este equipo falla por la red WiFi del emulador)
    // - Producción -> "https://tu-dominio.com/"
    private const val BASE_URL = "http://192.168.20.196:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val token = SessionManager.getToken()
        val request = if (!token.isNullOrBlank() && chain.request().header("Authorization") == null) {
            chain.request().newBuilder().addHeader("Authorization", token).build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: CaucaResilienteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CaucaResilienteApiService::class.java)


    }
}