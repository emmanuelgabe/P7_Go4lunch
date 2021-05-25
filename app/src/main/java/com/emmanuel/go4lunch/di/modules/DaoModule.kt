package com.emmanuel.go4lunch.di.modules

import android.content.Context
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import com.emmanuel.go4lunch.data.database.RestaurantDetailDatabase
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
open class DaoModule(context: Context) {
    val database = RestaurantDetailDatabase.getRestaurantDetailDatabase(context)
    @Singleton
    @Provides
    open fun provideRestaurantDetailDao(): RestaurantDetailDao {
        return database.restaurantDetailDao()
    }
}