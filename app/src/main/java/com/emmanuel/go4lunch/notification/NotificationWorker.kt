package com.emmanuel.go4lunch.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.utils.isSameDay
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.StringBuilder
import java.util.*

class NotificationWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId")
        val restaurantFavId = inputData.getString("restaurantId")
        val restaurantName = inputData.getString("restaurantName")
        val restaurantAddress = inputData.getString("restaurantAddress")
        val firestoreInstance = FirebaseFirestore.getInstance()
        val usersCollectionRef = firestoreInstance.collection(FirestoreService.COLLECTION_USERS)

        CoroutineScope(Dispatchers.IO).launch {
            val userDocuments = usersCollectionRef.get().await().documents
            val workmateParticipate = StringBuilder()
            for (document in userDocuments) {
                if (document.get("restaurantFavorite")?.toString()
                        .equals(restaurantFavId) && document.id != userId && isSameDay(
                        document.getTimestamp("favoriteDate")?.toDate(),
                        Calendar.getInstance().time
                    )
                )
                    workmateParticipate.append("${document.get("name").toString()},")
            }
            if (workmateParticipate.isNotEmpty())
                workmateParticipate.deleteCharAt(workmateParticipate.length - 1)
            if (workmateParticipate.isBlank()) {
                workmateParticipate.append("Nobody chose your restaurant")
            }
            launch(Dispatchers.Main) {
                val notificationHelper = NotificationHelper(
                    context,
                    restaurantName,
                    restaurantAddress,
                    workmateParticipate
                )
                notificationHelper.createNotification()
            }
        }
        return Result.success()
    }
}
