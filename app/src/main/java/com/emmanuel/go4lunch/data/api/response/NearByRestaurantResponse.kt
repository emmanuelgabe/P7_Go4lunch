package com.emmanuel.go4lunch.data.api.response

import com.emmanuel.go4lunch.data.api.model.NearByRestaurant

data class NearByRestaurantListResponse(
    var results: List<NearByRestaurant>?
)
data class NearByRestaurantDetailResponse(
    var result: NearByRestaurant?
)