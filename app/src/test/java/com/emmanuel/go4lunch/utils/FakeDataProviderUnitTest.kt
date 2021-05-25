@file:Suppress("UnstableApiUsage")

package com.emmanuel.go4lunch.utils

import com.emmanuel.go4lunch.data.api.response.NearByRestaurantDetailResponse
import com.emmanuel.go4lunch.data.api.response.NearByRestaurantListResponse
import com.google.common.reflect.TypeToken
import retrofit2.Response
import java.lang.reflect.Type

object FakeDataProviderUnitTest {

    private val nearRestaurantListType: Type =
        object : TypeToken<NearByRestaurantListResponse>() {}.type

    private val nearRestaurantDetailType: Type =
        object : TypeToken<NearByRestaurantDetailResponse>() {}.type

    val nearRestaurantList: NearByRestaurantListResponse =
        ParserUnitTest.parseJsonFile(nearRestaurantListType, "restaurant/list.json")

    val nearRestaurantDetail: NearByRestaurantDetailResponse =
        ParserUnitTest.parseJsonFile(nearRestaurantDetailType, "restaurant/detail.json")
}