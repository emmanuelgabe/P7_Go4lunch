package com.emmanuel.go4lunch.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emmanuel.go4lunch.MainActivity
import com.emmanuel.go4lunch.R
import com.emmanuel.go4lunch.utils.NOTIFICATION_LUNCH_CHANNEL_ID

internal class NotificationHelper(
    private val context: Context,
    private val restaurantName: String?,
    private val restaurantAddress: String?,
    private val workmatesParticipant: StringBuilder
) {
    fun createNotification() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(
                context.getString(
                    R.string.notification_text,
                    restaurantName,
                    restaurantAddress,
                    workmatesParticipant
                )
            )
            .setBigContentTitle(context.getString(R.string.notification_title))
            .setSummaryText(context.getString(R.string.app_name))

        val notificationBuilder =
            NotificationCompat.Builder(context, NOTIFICATION_LUNCH_CHANNEL_ID)

        notificationBuilder.apply {
            setSmallIcon(R.drawable.ic_your_lunch_24)
            setContentTitle(context.getString(R.string.notification_title))
            setStyle(bigTextStyle)
            setContentText(
                context.getString(
                    R.string.notification_text,
                    restaurantName,
                    restaurantAddress,
                    workmatesParticipant
                )
            )
            setAutoCancel(false)
            setContentIntent(resultPendingIntent)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            LUNCH_NOTIFICATION_ID.hashCode(), notificationBuilder.build()
        )
    }

    companion object {
        private const val LUNCH_NOTIFICATION_ID = "launchId"
    }
}