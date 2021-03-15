package com.emmanuel.go4lunch.ui.workmates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WorkmateViewModel(private val workmateRepository: WorkmateRepository) : ViewModel() {
    val restaurantLiveData = MutableLiveData<List<Restaurant>>()

    private var getAllRestaurantJob: Job? = null

    fun getAllRestaurants() {
        getAllRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getAllRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            workmateRepository.getAllRestaurants().addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val restaurantList = mutableListOf<Restaurant>()
                    for (document in snapshot.documents) {
                        restaurantList.add(
                            Restaurant(document.id, document.get("name").toString())
                        )
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        restaurantLiveData.value = restaurantList
                    }
                }
            }
        }
    }
}