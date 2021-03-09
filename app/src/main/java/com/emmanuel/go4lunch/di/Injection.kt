package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.google.firebase.firestore.FirebaseFirestore

object Injection {

    private fun provideRestaurantDataSource(): RestaurantRepository {
        val retrofitBuilder = RetrofitBuilder()
        return RestaurantRepository(retrofitBuilder)
    }

     fun provideWorkmateDataSource(): WorkmateRepository {
        val firestoreInstance = FirebaseFirestore.getInstance()
        val firestoreService = FirestoreService(firestoreInstance)
        return WorkmateRepository(firestoreService)
    }
    fun provideViewModelFactory(): ViewModelFactory {
        val dataSourceWorkmate = provideWorkmateDataSource()
        val dataSourceRestaurant = provideRestaurantDataSource()
        return ViewModelFactory(dataSourceRestaurant, dataSourceWorkmate)
    }
}
