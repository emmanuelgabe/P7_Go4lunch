package com.emmanuel.go4lunch.di

import android.content.Context
import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.RetrofitBuilder
import com.emmanuel.go4lunch.data.database.RestaurantDetailDatabase
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.google.firebase.firestore.FirebaseFirestore

object Injection {

/*    private fun provideRestaurantDataSource(context: Context): RestaurantRepository? {
        val retrofitBuilder = RetrofitBuilder()
        val restaurantDetailDatabase: RestaurantDetailDatabase = RestaurantDetailDatabase.getRestaurantDetailDatabase(context)
        return null
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

    fun provideViewModelFactory(context: Context): ViewModelFactory? {
        val dataSourceWorkmate = provideWorkmateDataSource()
        val dataSourceRestaurant = provideRestaurantDataSource(context)
        val dataSourcePlace = providePlaceDataSource()
        return null
    }*/
}
