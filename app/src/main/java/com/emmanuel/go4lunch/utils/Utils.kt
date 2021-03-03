package com.emmanuel.go4lunch.utils

import android.annotation.SuppressLint
import com.emmanuel.go4lunch.BuildConfig
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_PERMISSIONS_CODE_FINE_LOCATION: Int = 5000
const val REQUEST_PERMISSIONS_CODE_CALL_PHONE: Int = 5100
const val MAX_WITH_ICON = 60
const val MAX_WITH_IMAGE = 400
const val RADIUS = 500

fun getPhotoUrlFromReference(reference: String?, maxWith: Int): String {
    return "https://maps.googleapis.com/maps/api/place/photo?" +
            "maxwidth=$maxWith&" +
            "photoreference=$reference" +
            "&key=${BuildConfig.GOOGLE_MAP_API_KEY}"
}
@SuppressLint("SimpleDateFormat")
fun isSameDay(date1: Date?, date2: Date?): Boolean {
    if (date1 == null || date2 == null) return false
    val fmt = SimpleDateFormat("yyyyMMdd")
    return fmt.format(date1).equals(fmt.format(date2))
}