package com.emmanuel.go4lunch.di.modules

import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.di.ViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class ViewModelModule {
    @Singleton
    @Provides
    open fun provideRestaurantDetailViewModelFactory(
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