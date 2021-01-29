package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.data.api.FirebaseWorkmateService

object Injection {

    private val service = FirebaseWorkmateService

    fun getFirebaseWorkmateService(): FirebaseWorkmateService {
        return service
    }
}
