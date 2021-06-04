package com.emmanuel.go4lunch.data.repository

import android.location.Location
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.emmanuel.go4lunch.data.api.response.PlaceAutoComplete
import com.emmanuel.go4lunch.utils.PLACE_OFFSET

open class PlaceRepository(private val googleMapsService: GoogleMapsService) {

    suspend fun getPlaces(
        input: String,
        location: Location,
        radius: Int,
        types: String = "establishment",
        offset: Int = PLACE_OFFSET
    ): PlaceAutoComplete? {
        val response = googleMapsService.getPlaces(
            offset.toString(),
            input,
            types,
            "${location.latitude},${location.longitude}",
            radius,
            BuildConfig.GOOGLE_MAP_API_KEY
        )
        return response.body()
    }
}