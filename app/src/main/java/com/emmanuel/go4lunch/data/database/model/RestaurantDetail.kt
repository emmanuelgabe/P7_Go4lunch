package com.emmanuel.go4lunch.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RestaurantDetail(
    @PrimaryKey val id: String,
    @ColumnInfo val name: String?,
    @ColumnInfo(name = "business_status") val businessStatus: String?,
    @ColumnInfo val rating: Double?,
    @ColumnInfo(name = "ratings_number") val ratingNumber: Int?,
    @ColumnInfo val address: String?,
    @ColumnInfo val price: Int?,
    @ColumnInfo(name = "photo_reference") val photoReference: String?,
    @ColumnInfo val lat: Double,
    @ColumnInfo val lng: Double,
    @ColumnInfo(name = "formatted_number") val phoneNumber: String?,
    @ColumnInfo(name = "open_now") val openNow: Boolean?,
    @ColumnInfo(name = "creation_time_stamp") val creationTimestamp: Long,
    @ColumnInfo val website: String?,
    @ColumnInfo(name = "weekday_text_1") val weekdayText1: String?,
    @ColumnInfo(name = "weekday_text_2") val weekdayText2: String?,
    @ColumnInfo(name = "weekday_text_3") val weekdayText3: String?,
    @ColumnInfo(name = "weekday_text_4") val weekdayText4: String?,
    @ColumnInfo(name = "weekday_text_5") val weekdayText5: String?,
    @ColumnInfo(name = "weekday_text_6") val weekdayText6: String?,
    @ColumnInfo(name = "weekday_text_7") val weekdayText7: String?
)
