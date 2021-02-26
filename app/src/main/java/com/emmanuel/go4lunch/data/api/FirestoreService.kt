package com.emmanuel.go4lunch.data.api

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirestoreService {

    private val firestoreDB = FirebaseFirestore.getInstance()

    // --- COLLECTION REFERENCE ---
    private val usersCollectionRef = firestoreDB.collection(COLLECTION_USERS)
    private val restaurantsCollection = firestoreDB.collection(COLLECTION_RESTAURANTS)

    fun getAllUser(): Task<QuerySnapshot> {
        return usersCollectionRef.get()
    }

    fun getUser(id: String): Task<DocumentSnapshot> {
        return usersCollectionRef.document(id).get()
    }

    fun updateUser(id: String, user: Map<String, Any?>) {
        usersCollectionRef.document(id).update(user)
    }

    fun createUser(id: String, user: Map<String, Any?>) {
        usersCollectionRef.document(id).set(user)
    }

    fun getAllRestaurants(): Task<QuerySnapshot> {
        return restaurantsCollection.get()
    }

    fun createRestaurant(id: String, restaurant: Map<String, Any?>) {
        restaurantsCollection.document(id).set(restaurant)
    }

    fun deleteRestaurant(id: String) {
        restaurantsCollection.document(id).delete()
    }

    companion object {
        const val COLLECTION_RESTAURANTS = "restaurants"
        const val COLLECTION_USERS = "users"
    }
}