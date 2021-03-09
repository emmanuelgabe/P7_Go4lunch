package com.emmanuel.go4lunch.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {

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

    companion object {
        private const val GOOGLE_MAP_BASE_URL = "https://maps.googleapis.com"
    }
}