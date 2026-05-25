package com.example.harvestlinkapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    //Emulator or phone switch
    private const val USE_EMULATOR = true

    // Emulator → talks to your laptop's localhost via special alias 10.0.2.2
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"


  // replace with  real IPv4 from `ipconfig`.

    private const val PHONE_BASE_URL = "http://192.168.0.23:3000/"

    private val BASE_URL: String
        get() = if (USE_EMULATOR) EMULATOR_BASE_URL else PHONE_BASE_URL

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
