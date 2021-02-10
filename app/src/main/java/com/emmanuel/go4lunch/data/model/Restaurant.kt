package com.emmanuel.go4lunch.data.model

data class Restaurant(
    var id: String,
    var name: String?,
    var lat: Double,
    var lng: Double,
    var businessStatus: String?,
    var address: String?,
    var timetable: String?,
    var photoUrl: String?,
    var price: Int?, // 0 to 4
    var rating: Double?, // 1.0 to 5.0
    var ratingNumber: Int?,
    var phoneNumber: Int?,
    var website: Int?
) {
    constructor(
        id: String,
        lat: Double,
        lng: Double
    ) : this(id, null, lat, lng, null, null, null, null, null, null, null, null, null)
}