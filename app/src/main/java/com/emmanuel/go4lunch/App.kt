package com.emmanuel.go4lunch


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.multidex.MultiDexApplication
import com.emmanuel.go4lunch.di.AppComponent
import com.emmanuel.go4lunch.di.DaggerAppComponent
import com.emmanuel.go4lunch.utils.NOTIFICATION_LUNCH_CHANNEL_ID
import com.google.android.libraries.places.api.Places

open class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidSDKs()
        setUpChannel()
       initDagger()
    }

    private fun initDagger(): AppComponent {
        return DaggerAppComponent.create()
    }


    private fun initializeAndroidSDKs() {
        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAP_API_KEY)
    }

    private fun setUpChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_LUNCH_CHANNEL_ID,
                "Lunch channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "description de la chaine"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                setShowBadge(false)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}