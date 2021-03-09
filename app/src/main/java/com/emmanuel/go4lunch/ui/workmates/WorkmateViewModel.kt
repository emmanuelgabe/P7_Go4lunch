package com.emmanuel.go4lunch.ui.workmates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkmateViewModel(private val workmateRepository: WorkmateRepository) : ViewModel() {
    val workmatesLiveData = MutableLiveData<List<Workmate>>()
    val restaurantLiveData = MutableLiveData<List<Restaurant>>()

    private var getAllRestaurantJob: Job? = null
    private var getAllWorkmateJob: Job? = null

    fun getAllWorkmate() {
        getAllWorkmateJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getAllWorkmateJob = viewModelScope.launch(Dispatchers.IO) {
            val workmates = workmateRepository.getAllWorkmate()
            withContext(Dispatchers.Main) {
                workmatesLiveData.value = workmates
            }
        }
    }

    fun getAllRestaurants() {
        getAllRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getAllRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            val restaurants = workmateRepository.getAllRestaurants()
            withContext(Dispatchers.Main) {
                restaurantLiveData.value = restaurants
            }
        }

    }
}