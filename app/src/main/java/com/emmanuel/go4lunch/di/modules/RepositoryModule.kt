package com.emmanuel.go4lunch.di.modules

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class RepositoryModule {

    @Singleton
    @Provides
    open fun provideWorkmateRepository(fireStoreService: FirestoreService): WorkmateRepository {
        return WorkmateRepository(fireStoreService)
    }

    @Singleton
    @Provides
    open fun provideRestaurantRepository(
        googleMapsService: GoogleMapsService,
        restaurantDetailDao: RestaurantDetailDao
    ): RestaurantRepository {
        return RestaurantRepository(googleMapsService, restaurantDetailDao)
    }

    @Singleton
    @Provides
    open fun providePlaceRepository(googleMapsService: GoogleMapsService): PlaceRepository {
        return PlaceRepository(googleMapsService)
    }
}