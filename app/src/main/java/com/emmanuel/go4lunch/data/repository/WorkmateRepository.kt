package com.emmanuel.go4lunch.data.repository

import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.di.Injection
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class WorkmateRepository {

    private val service = Injection.getFirebaseWorkmateService()

    fun createUser(user: Workmate) {
        service.createUser(user)
    }

    fun getUser(uid: String): Task<DocumentSnapshot> {
        return service.getUser(uid)
    }
}