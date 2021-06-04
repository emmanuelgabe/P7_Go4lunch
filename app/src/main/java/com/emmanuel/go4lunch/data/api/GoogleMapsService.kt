package com.emmanuel.go4lunch.data.api


import com.emmanuel.go4lunch.data.api.response.NearByRestaurantDetailResponse
import com.emmanuel.go4lunch.data.api.response.NearByRestaurantListResponse
import com.emmanuel.go4lunch.data.api.response.PlaceAutoComplete
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsService {
    @GET("/maps/api/place/details/json")
    suspend fun getDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,
        @Query("key") key: String
    ): NearByRestaurantDetailResponse

    @GET("/maps/api/place/nearbysearch/json")
    suspend fun getNearRestaurant(
        @Query("location", encoded = false) location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): NearByRestaurantListResponse

    @GET("/maps/api/place/nearbysearch/json")
    suspend fun getNearRestaurantNextPage(
        @Query("location", encoded = false) location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String,
        @Query("pagetoken") pageToken: String
    ): NearByRestaurantListResponse

    @GET("/maps/api/place/autocomplete/json")
    suspend fun getPlaces(
        @Query("offset") offset: String,
        @Query("input") input: String,
        @Query("types") types: String,
        @Query("location", encoded = false) location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Response<PlaceAutoComplete>
}
