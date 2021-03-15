package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.google.android.libraries.places.api.model.Place
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

    private fun providePlaceDataSource(): PlaceRepository {
        val retrofitBuilder = RetrofitBuilder()
        return PlaceRepository(retrofitBuilder)
    }

    fun provideViewModelFactory(): ViewModelFactory {
        val dataSourceWorkmate = provideWorkmateDataSource()
        val dataSourceRestaurant = provideRestaurantDataSource()
        val dataSourcePlace = providePlaceDataSource()

        return ViewModelFactory(dataSourceRestaurant, dataSourceWorkmate, dataSourcePlace)
    }
}
