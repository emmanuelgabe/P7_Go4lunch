package com.emmanuel.go4lunch.data.repository

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.utils.UpdateCurrentUserEvent
import com.emmanuel.go4lunch.utils.UpdateWorkmatesEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import org.greenrobot.eventbus.EventBus

class WorkmateRepository(private val firestoreService: FirestoreService) {

    fun createWorkmate(user: Workmate) {
        val userDataMap = mapOf(
            "email" to user.email,
            "name" to user.name,
            "avatarURL" to user.avatarURL,
            "restaurantsLike" to user.restaurantsIdLike,
            "restaurantFavorite" to user.restaurantFavorite
        )
        firestoreService.createUser(user.uid, userDataMap)
    }

    fun getAllRestaurants(): CollectionReference {
        return firestoreService.getAllRestaurants()
    }

    fun updateWorkmate(workmate: Workmate) {
        val workmateDataMap = mapOf(
            "email" to workmate.email,
            "name" to workmate.name,
            "avatarURL" to workmate.avatarURL,
            "restaurantsLike" to workmate.restaurantsIdLike,
            "restaurantFavorite" to workmate.restaurantFavorite,
            "favoriteDate" to workmate.favoriteDate
        )
        firestoreService.updateUser(workmate.uid, workmateDataMap)
    }

    fun addRestaurant(restaurant: Restaurant) {
        firestoreService.createRestaurant(
            restaurant.id,
            mapOf<String, Any?>("name" to restaurant.name)
        )
    }

    fun deleteRestaurant(id: String) {
        firestoreService.deleteRestaurant(id)
    }

    fun deleteWorkmate(id: String) {
        firestoreService.deleteUser(id)
    }

    fun addWorkmatesSnapshotListener() {
        firestoreService.getAllUserCollectionReference().addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val workmateList = mutableListOf<Workmate>()
                for (document in snapshot.documents) {
                    @Suppress("UNCHECKED_CAST")
                    val workmate = Workmate(
                        document.id,
                        document.get("email").toString(),
                        document.get("name").toString(),
                        document.get("avatarURL").toString(),
                        document.get("restaurantsLike") as? List<String>?,
                        document.get("restaurantFavorite")?.toString(),
                        document.getTimestamp("favoriteDate")?.toDate()
                    )
                    workmateList.add(workmate)

                    if (document.id == FirebaseAuth.getInstance().uid) {
                        EventBus.getDefault().post(UpdateCurrentUserEvent(workmate))
                    }
                }
                EventBus.getDefault().post(UpdateWorkmatesEvent(workmateList))
            }
        }
    }
}
