package com.emmanuel.go4lunch.data.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class NearByRestaurant(
    @SerializedName("place_id")
    val placeId: String,
    val name: String?,
    @SerializedName("business_status")
    val businessStatus: String?,
    val rating: Double?,
    @SerializedName("user_ratings_total")
    val ratingNumber: Int?,
    @SerializedName("vicinity")
    val address: String?,
    @SerializedName("price_level")
    val price: Int?,
    val photos: List<Photos>?,
    val geometry: Geometry, // contain location
    @SerializedName("opening_hours")
    val openingHours: OpeningHours?,
    @SerializedName("formatted_phone_number")
    val phoneNumber: String?,
    val website: String?,
): Serializable

data class Geometry(
    val location: Location
): Serializable

data class Location(
    val lat: Double,
    val lng: Double
): Serializable

data class Photos(
    @SerializedName("photo_reference")
    val photoReference: String?
): Serializable

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean?,
    @SerializedName("weekday_text")
    val weekdayText: List<String>
): Serializable