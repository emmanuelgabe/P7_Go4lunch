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
){
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass)
            return false
        other as Workmate
        when {
            uid != other.uid -> return false
            name != other.name -> return false
            avatarURL != other.avatarURL -> return false
            restaurantFavorite != other.restaurantFavorite -> return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (avatarURL?.hashCode() ?: 0)
        result = 31 * result + (restaurantsIdLike?.hashCode() ?: 0)
        result = 31 * result + (restaurantFavorite?.hashCode() ?: 0)
        result = 31 * result + (favoriteDate?.hashCode() ?: 0)
        return result
    }
}