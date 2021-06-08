package com.emmanuel.go4lunch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.multidex.MultiDexApplication
import com.emmanuel.go4lunch.di.AppComponent
import com.emmanuel.go4lunch.di.DaggerAppComponent
import com.emmanuel.go4lunch.di.modules.DaoModule
import com.emmanuel.go4lunch.utils.NOTIFICATION_LUNCH_CHANNEL_ID
import com.google.android.libraries.places.api.Places

open class App : MultiDexApplication() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        application = this
        initializeAndroidSDKs()
        setUpChannel()
        appComponent = initDagger()
    }

    protected open fun initDagger(): AppComponent {
        return DaggerAppComponent.builder()
            .daoModule(DaoModule(applicationContext))
            .build()
    }

    fun appComponent() = appComponent

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
                description = "lunch channel"
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
    companion object {
        lateinit var application:App
        fun app() = application
    }
}