package com.emmanuel.go4lunch

import androidx.lifecycle.ViewModel
import com.emmanuel.go4lunch.data.repository.RestaurantRepository

class MainViewModel(private val restaurantRepo: RestaurantRepository) : ViewModel() {
    init {

    }

    fun getRestaurantId(){
    //   return restaurantRepo.getNearRestaurantId().body().
    }
}