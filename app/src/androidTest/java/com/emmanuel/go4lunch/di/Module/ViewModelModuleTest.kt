package com.emmanuel.go4lunch.di.Module

import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.di.ViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModuleTest {
    @Singleton
    @Provides
    fun provideRestaurantDetailViewModelFactory(
        restaurantRepository: RestaurantRepository,
        workmateRepository: WorkmateRepository,
        placeRepository: PlaceRepository
    ): ViewModelFactory {
        return ViewModelFactory(
            restaurantRepository,
            workmateRepository,
            placeRepository
        )
    }
}