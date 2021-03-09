package com.emmanuel.go4lunch.data.repository

import android.location.Location
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.utils.RADIUS

class RestaurantRepository(private val retrofitBuilder: RetrofitBuilder) {

    suspend fun getAllNearRestaurant(
        lastKnownLocation: Location?,
        radius: Int = RADIUS
    ): List<NearByRestaurant>? {
        val response =
            retrofitBuilder.googleMapsService.getNearRestaurant(
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
        val response = retrofitBuilder.googleMapsService.getDetails(
            restaurantsId,
            fields.joinToString(separator = ","),
            BuildConfig.GOOGLE_MAP_API_KEY
        )
        return response.body()?.result
    }

    suspend fun getAllDetailRestaurant(lastKnownLocation: Location?): List<NearByRestaurant> {
        val restaurants = this.getAllNearRestaurant(lastKnownLocation)
        val restaurantsDetailList = mutableListOf<NearByRestaurant>()
        restaurants?.let {
            for (restaurant in restaurants) {
                restaurantsDetailList.add(getDetailRestaurant(restaurant.placeId)!!)
            }
        }
        return restaurantsDetailList
    }
}