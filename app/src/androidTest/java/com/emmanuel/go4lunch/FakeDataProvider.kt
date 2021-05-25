package com.emmanuel.go4lunch

import com.emmanuel.go4lunch.utils.Parser
import com.emmanuel.go4lunch.data.api.response.NearByRestaurantDetailResponse
import com.emmanuel.go4lunch.data.api.response.NearByRestaurantListResponse
import com.google.gson.reflect.TypeToken
import retrofit2.Response
import java.lang.reflect.Type

object FakeDataProvider {
    private val nearRestaurantListType: Type =
        object : TypeToken<Response<NearByRestaurantListResponse>>() {}.type
    private val nearRestaurantDetailType: Type =
        object : TypeToken<Response<NearByRestaurantDetailResponse>>() {}.type
    val nearRestaurantList: Response<NearByRestaurantListResponse> =
        Parser.parseJsonFile(nearRestaurantListType, "restaurant/list.json")
    val nearRestaurantDetail: Response<NearByRestaurantDetailResponse> =
        Parser.parseJsonFile(nearRestaurantDetailType, "restaurant/detail.json")
}