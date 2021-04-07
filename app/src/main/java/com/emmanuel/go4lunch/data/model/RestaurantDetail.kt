package com.emmanuel.go4lunch.data.model

data class RestaurantDetail(
    var id: String,
    var name: String?,
    val businessStatus: String? = null,
    val rating: Double? = null,
    val ratingNumber: Int? = null,
    val address: String? = null,
    val photoReference: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val openNow: Boolean? = null,
    val creationTimestamp: Long? = null,
    val weekdayText1: String? = null,
    val weekdayText2: String? = null,
    val weekdayText3: String? = null,
    val weekdayText4: String? = null,
    val weekdayText5: String? = null,
    val weekdayText6: String? = null,
    val weekdayText7: String? = null,
    var workmateCount: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass)
            return false
        other as RestaurantDetail
        when {
            id != other.id -> return false
            creationTimestamp != other.creationTimestamp -> return false
            workmateCount != other.workmateCount -> return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (businessStatus?.hashCode() ?: 0)
        result = 31 * result + (rating?.hashCode() ?: 0)
        result = 31 * result + (ratingNumber ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (photoReference?.hashCode() ?: 0)
        result = 31 * result + (lat?.hashCode() ?: 0)
        result = 31 * result + (lng?.hashCode() ?: 0)
        result = 31 * result + (openNow?.hashCode() ?: 0)
        result = 31 * result + (creationTimestamp?.hashCode() ?: 0)
        result = 31 * result + (weekdayText1?.hashCode() ?: 0)
        result = 31 * result + (weekdayText2?.hashCode() ?: 0)
        result = 31 * result + (weekdayText3?.hashCode() ?: 0)
        result = 31 * result + (weekdayText4?.hashCode() ?: 0)
        result = 31 * result + (weekdayText5?.hashCode() ?: 0)
        result = 31 * result + (weekdayText6?.hashCode() ?: 0)
        result = 31 * result + (weekdayText7?.hashCode() ?: 0)
        result = 31 * result + (workmateCount ?: 0)
        return result
    }
}