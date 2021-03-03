package com.emmanuel.go4lunch


import androidx.multidex.MultiDexApplication
import com.google.android.libraries.places.api.Places

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidSDKs()
    }

    private fun initializeAndroidSDKs() {
        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAP_API_KEY)
    }
}