package com.emmanuel.go4lunch.data.model

import java.util.*

data class Workmate(
    val uid: String,
    var email: String?,
    var name: String?,
    var avatarURL: String?,
    var restaurantsIdLike: List<String>? = null,
    var restaurantFavorite: String? = null,
    var favoriteDate: Date? = null
)