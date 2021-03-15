package com.emmanuel.go4lunch.data.repository

import android.location.Location
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.api.response.PlaceAutoComplete
import com.emmanuel.go4lunch.utils.PLACE_OFFSET
import com.emmanuel.go4lunch.utils.RADIUS

class PlaceRepository(private val retrofitBuilder: RetrofitBuilder) {

    suspend fun getPlaces(
        input: String,
        location: Location,
        radius: Int = RADIUS,
        types: String = "establishment",
        offset: Int = PLACE_OFFSET
    ): PlaceAutoComplete? {
        val response = retrofitBuilder.googleMapsService.getPlaces(
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