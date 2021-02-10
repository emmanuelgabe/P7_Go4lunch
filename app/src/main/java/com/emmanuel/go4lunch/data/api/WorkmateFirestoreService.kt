package com.emmanuel.go4lunch.data.api

import android.util.Log

import com.emmanuel.go4lunch.data.model.Workmate
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


object WorkmateFirestoreService {
    private const val TAG = "WorkmateService"
    private const val COLLECTION_USERS = "users"

    // TODO move db in external class
    private val db = FirebaseFirestore.getInstance()

    // --- COLLECTION REFERENCE ---
    private val usersCollectionRef = db.collection(COLLECTION_USERS)

    fun createUser(workmate: Workmate) {
        val userDataMap =
            mapOf<String, Any?>(
                "email" to workmate.email,
                "name" to workmate.name,
                "avatarURL" to workmate.avatarURL
            )
        usersCollectionRef.document(workmate.uid).set(userDataMap)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }
    }


    fun getUser(uid: String?): Task<DocumentSnapshot> {
        return usersCollectionRef.document(uid.toString()).get()
    }
/*
    fun getUser(uid: String): Workmate {
        usersCollectionRef.document(uid).get()


    }*/

    fun updateUsername(workmate: Workmate) {
        val userDataMap =
            mapOf<String, Any?>(
                "email" to workmate.email,
                "name" to workmate.name,
                "avatarURL" to workmate.avatarURL
            )

        usersCollectionRef.document(workmate.uid).update(userDataMap)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun deleteUser(uid: String) {
        usersCollectionRef.document(uid).delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }
}