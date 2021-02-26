package com.emmanuel.go4lunch.data.model

data class Workmate(
    val uid: String,
    var email: String?,
    var name: String?,
    var avatarURL: String?,
    var restaurantsIdLike: List<String>? = null,
    var restaurantFavorite: String? = null
)