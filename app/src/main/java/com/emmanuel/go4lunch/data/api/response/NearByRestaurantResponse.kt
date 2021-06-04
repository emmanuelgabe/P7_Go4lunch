package com.emmanuel.go4lunch.data.api.response

import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.google.gson.annotations.SerializedName

data class NearByRestaurantListResponse(
    var results: MutableList<NearByRestaurant>,
    @SerializedName("next_page_token")
    var nextPageToken: String?
)

data class NearByRestaurantDetailResponse(
    var result: NearByRestaurant?
)