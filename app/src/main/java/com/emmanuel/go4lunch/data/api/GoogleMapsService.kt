package com.emmanuel.go4lunch.data.api


import com.emmanuel.go4lunch.data.api.response.NearByRestaurantDetailResponse
import com.emmanuel.go4lunch.data.api.response.NearByRestaurantListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface GoogleMapsService {
    @GET("/maps/api/place/details/json")
    suspend fun getDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,
        @Query("key") key: String
    ): Response<NearByRestaurantDetailResponse>


    // TODO suspend
    @GET("/maps/api/place/nearbysearch/json")
    suspend fun getNearRestaurantId(
        @Query("location", encoded = false) location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): Response<NearByRestaurantListResponse>
}
