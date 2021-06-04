package com.emmanuel.go4lunch.di.Module

import android.content.Context
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import com.emmanuel.go4lunch.data.database.RestaurantDetailDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DaoModuleTest(context: Context) {
    val dataBase = RestaurantDetailDatabase.getRestaurantDetailDatabase(context)
    @Singleton
    @Provides
    fun provideRestaurantDetailDao(): RestaurantDetailDao {
        return dataBase.restaurantDetailDao()
    }
}