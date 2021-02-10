package com.emmanuel.go4lunch.data.api.model

import com.google.gson.annotations.SerializedName


data class NearByRestaurant(
    @SerializedName("place_id")
    val placeId: String,
    val name: String?,
    @SerializedName("business_status")
    val businessStatus: String?,
    val rating: String?,
    @SerializedName("user_ratings_total")
    val ratingNumber: String?,
    @SerializedName("vicinity")
    val address: String?,
    @SerializedName("price_level")
    val price: String?,
    @SerializedName("Photos")
// todo get photo with ref
// https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=ATtYBwLF1DMuzV7aI7XKH81Gws1OBFA_qthXWle3esXPQm96bE-oAs9o3CbPpXMSxi3Hof1JP6P08t-yNgnNrUTEDIlCEvJbpuhhe0LmrF670RNIAef8trgaov8urIN1lqZcSi8qMI5ipCMiT4Q4jgtCApBMq8RU92HjIJm9y-hSyVbjzThZ&key=
    val photos: Photos?,
    val geometry: Geometry, // contain location
    @SerializedName("opening_hours")
    val openingHours: OpeningHours?,
    @SerializedName("formatted_phone_number")
    val phoneNumber: String?,
    val website: String?,
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Photos(
    @SerializedName("photo_reference")
    val photoReference: String?
)

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean?,
    @SerializedName("weekday_text")
    val weekdayText: List<WeekdayText>
)

data class WeekdayText(
    val timestampPerDay: String?
)