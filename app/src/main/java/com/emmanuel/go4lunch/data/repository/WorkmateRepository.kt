package com.emmanuel.go4lunch.data.repository

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import kotlinx.coroutines.tasks.await

object WorkmateRepository {


    fun createWorkmate(user: Workmate) {
        val userDataMap = mapOf(
            "email" to user.email,
            "name" to user.name,
            "avatarURL" to user.avatarURL,
            "restaurantsLike" to user.restaurantsIdLike,
            "restaurantFavorite" to user.restaurantFavorite
        )
        FirestoreService().createUser(user.uid, userDataMap)
    }

    suspend fun getUser(uid: String): Workmate {
        val document = FirestoreService().getUser(uid).await().data
        return Workmate(
            uid, document!!["email"].toString(),
            document["name"].toString(), document["avatarURL"].toString()
        )
    }

    suspend fun getAllWorkmate(): List<Workmate> {
        val workmateList = mutableListOf<Workmate>()
        val documents = FirestoreService().getAllUser().await().documents
        for (document in documents) {
            @Suppress("UNCHECKED_CAST")
            workmateList.add(
                Workmate(
                    document.id,
                    document.get("email").toString(),
                    document.get("name").toString(),
                    document.get("avatarURL").toString(),
                    document.get("restaurantsLike") as? List<String>?,
                    document.get("restaurantFavorite")?.toString()
                )
            )
        }
        return workmateList.toList()
    }

    suspend fun getAllRestaurants(): List<Restaurant> {
        val restaurantList = mutableListOf<Restaurant>()
        val documents = FirestoreService().getAllRestaurants().await().documents
        for (document in documents) {
            restaurantList.add(
                Restaurant(document.id, document.get("name").toString())
            )
        }
        return restaurantList.toList()
    }

    fun updateWorkmate(workmate: Workmate) {
        val workmateDataMap = mapOf(
            "email" to workmate.email,
            "name" to workmate.name,
            "avatarURL" to workmate.avatarURL,
            "restaurantsLike" to workmate.restaurantsIdLike,
            "restaurantFavorite" to workmate.restaurantFavorite
        )
        FirestoreService().updateUser(workmate.uid, workmateDataMap)
    }

    fun addRestaurant(restaurant: Restaurant) {
        FirestoreService().createRestaurant(
            restaurant.id,
            mapOf<String, Any?>("name" to restaurant.name)
        )
    }

    fun deleteRestaurant(id: String) {
        FirestoreService().deleteRestaurant(id)
    }
}