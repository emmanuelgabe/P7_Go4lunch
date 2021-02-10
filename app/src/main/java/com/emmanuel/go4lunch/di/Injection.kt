package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.data.api.WorkmateFirestoreService

object Injection {

    private val service = WorkmateFirestoreService

    fun getFirestoreWorkmateService(): WorkmateFirestoreService {
        return service
    }
}
