package com.emmanuel.go4lunch.data.api

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreService(fireStore: FirebaseFirestore) {

    // --- COLLECTION REFERENCE ---
    private val usersCollectionRef = fireStore.collection(COLLECTION_USERS)
    private val restaurantsCollectionRef = fireStore.collection(COLLECTION_RESTAURANTS)

    fun updateUser(id: String, user: Map<String, Any?>) {
        usersCollectionRef.document(id).update(user)
    }

    fun createUser(id: String, user: Map<String, Any?>) {
        usersCollectionRef.document(id).set(user)
    }

    fun deleteUser(id: String){
        usersCollectionRef.document(id).delete()
    }

    fun getAllRestaurants(): CollectionReference {
        return restaurantsCollectionRef
    }

    fun createRestaurant(id: String, restaurant: Map<String, Any?>) {
        restaurantsCollectionRef.document(id).set(restaurant)
    }

    fun deleteRestaurant(id: String) {
        restaurantsCollectionRef.document(id).delete()
    }

    fun getAllUserCollectionReference(): CollectionReference {
        return usersCollectionRef
    }

    companion object {
        const val COLLECTION_RESTAURANTS = "restaurants"
        const val COLLECTION_USERS = "users"
    }

}