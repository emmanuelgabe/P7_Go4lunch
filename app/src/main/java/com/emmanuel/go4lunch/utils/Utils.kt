package com.emmanuel.go4lunch.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.emmanuel.go4lunch.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_PERMISSIONS_CODE_FINE_LOCATION: Int = 5000
const val REQUEST_PERMISSIONS_CODE_CALL_PHONE: Int = 5100
const val MAX_WITH_ICON = 60
const val MAX_WITH_IMAGE = 400
const val RADIUS = 1000
const val PLACE_OFFSET = 2

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

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}