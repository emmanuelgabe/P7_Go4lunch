package com.emmanuel.go4lunch.ui.listview

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListViewViewModel(
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {
    val restaurantsDetailLiveData = MutableLiveData<List<NearByRestaurant>>()

    private var getRestaurantJob: Job? = null

    fun getAllDetailRestaurant(location: Location?) {
        getRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            val restaurantsDetail = restaurantRepository.getAllDetailRestaurant(location)
            withContext(Dispatchers.Main) {
                restaurantsDetailLiveData.value = restaurantsDetail
            }
        }
    }
}