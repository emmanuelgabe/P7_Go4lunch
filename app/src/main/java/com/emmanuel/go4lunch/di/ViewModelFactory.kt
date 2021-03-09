package com.emmanuel.go4lunch.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emmanuel.go4lunch.MainViewModel
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.ui.listview.ListViewViewModel
import com.emmanuel.go4lunch.ui.mapview.MapViewViewModel
import com.emmanuel.go4lunch.ui.restaurantdetail.RestaurantDetailViewModel
import com.emmanuel.go4lunch.ui.workmates.WorkmateViewModel
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val restaurantRepository: RestaurantRepository,
    private val workmateRepository: WorkmateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewViewModel::class.java)) {
            return ListViewViewModel(restaurantRepository, workmateRepository) as T
        }
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel::class.java)) {
            return RestaurantDetailViewModel(restaurantRepository, workmateRepository) as T
        }
        if (modelClass.isAssignableFrom(WorkmateViewModel::class.java)) {
            return WorkmateViewModel(workmateRepository) as T
        }
        if (modelClass.isAssignableFrom(MapViewViewModel::class.java)) {
            return MapViewViewModel(restaurantRepository, workmateRepository) as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(workmateRepository) as T
        }
        throw IllegalArgumentException("no view model class")
    }
}