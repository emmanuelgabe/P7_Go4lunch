package com.emmanuel.go4lunch.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    private const val GOOGLE_MAP_BASE_URL = "https://maps.googleapis.com"

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_MAP_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    val googleMapsService: GoogleMapsService by lazy {
        retrofitBuilder
            .build()
            .create(GoogleMapsService::class.java)
    }
}