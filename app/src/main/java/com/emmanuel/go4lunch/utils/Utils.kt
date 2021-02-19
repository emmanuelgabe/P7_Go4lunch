package com.emmanuel.go4lunch.utils

import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.RetrofitBuilder


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