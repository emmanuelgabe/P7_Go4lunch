package com.emmanuel.go4lunch.data.repository

import android.location.Location
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant

object RestaurantRepository {

    suspend fun getAllNearRestaurant(
        lastKnownLocation: Location?,
        radius: Int = 100
    ): List<NearByRestaurant>? {
        val response =
            RetrofitBuilder.googleMapsService.getNearRestaurant(
                "${lastKnownLocation?.latitude},${lastKnownLocation?.longitude}", radius,
                "restaurant",
                BuildConfig.GOOGLE_MAP_API_KEY
            )
        return response.body()?.results
    }

    suspend fun getDetailRestaurant(restaurantsId: String): NearByRestaurant? {
        val fields = listOf(
            "place_id", "name", "business_status", "rating", "user_ratings_total",
            "vicinity", "formatted_phone_number", "price_level", "geometry",
            "photo", "opening_hours", "formatted_phone_number", "website"
        )
        val response = RetrofitBuilder.googleMapsService.getDetails(
            restaurantsId,
            fields.joinToString(separator = ","),
            BuildConfig.GOOGLE_MAP_API_KEY
        )
        return response.body()?.result
    }

    suspend fun getAllDetailRestaurant(restaurantsId: List<NearByRestaurant>?): MutableList<NearByRestaurant> {
        val restaurantsDetailList = mutableListOf<NearByRestaurant>()
        if (restaurantsId?.size != 0 && restaurantsId != null) {
            for (i in 0 until restaurantsId.count()) {
                restaurantsDetailList.add(
                    getDetailRestaurant(restaurantsId.get(i).placeId)!!
                )
            }
        }
        return restaurantsDetailList
    }

}