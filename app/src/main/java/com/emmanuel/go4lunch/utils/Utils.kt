package com.emmanuel.go4lunch.utils

import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.RetrofitBuilder


const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 5000

fun getPhotoUrl(photoReference: String, maxWith: Int): String =
    "https://maps.googleapis.com/maps/api/place/photo" +
            "?maxwidth=$maxWith" +
            "&photoreference=$photoReference" +
            "&key=${BuildConfig.GOOGLE_MAP_API_KEY}"