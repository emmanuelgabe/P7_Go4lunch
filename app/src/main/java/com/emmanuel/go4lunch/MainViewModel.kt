package com.emmanuel.go4lunch

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.api.response.Prediction
import com.emmanuel.go4lunch.data.database.model.RestaurantDetailEntity
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.utils.UpdateCurrentUserEvent
import com.emmanuel.go4lunch.utils.UpdateWorkmatesEvent
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainViewModel(
    private val workmateRepository: WorkmateRepository,
    private val placeRepository: PlaceRepository,
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {
    init {
        EventBus.getDefault().register(this)
    }

    var lastKnownLocation: MutableLiveData<Location> = MutableLiveData<Location>()
    val placesAutocompleteLiveData = MutableLiveData<List<Prediction>>()

    val textSearchInput = MutableLiveData<String>()
    val workmatesLiveData = MutableLiveData<List<Workmate>>()
    val currentUserLiveData = MutableLiveData<Workmate>()
    val restaurantsDetailLiveData = MutableLiveData<List<RestaurantDetailEntity>>()
    val nearRestaurantsLiveData = MutableLiveData<List<NearByRestaurant>>()

    private var getNearRestaurantJob: Job? = null
    private var placeJob: Job? = null
    private var getWorkmateJob: Job? = null
    private var getRestaurantJob: Job? = null

    fun saveLocation(location: Location?) {
        this.lastKnownLocation.postValue(location)
    }

    fun setInputTextSearch(input: String) {
        textSearchInput.postValue(input)
    }

    fun getPlaces(input: String, radius: Int) {
        if (lastKnownLocation.value != null) {
            placeJob?.let {
                if (it.isActive) {
                    it.cancel()
                }
            }
            placeJob = viewModelScope.launch(Dispatchers.IO) {
                if (input.length > 3) {

                    val placeResponse =
                        placeRepository.getPlaces(input, lastKnownLocation.value!!, radius)
                    if (placeResponse != null) {
                        placesAutocompleteLiveData.postValue(placeResponse.predictions)
                    }
                }
            }
        }
    }

    fun addWorkmatesSnapshotListener() {
        getWorkmateJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getWorkmateJob = viewModelScope.launch(Dispatchers.IO) {
            workmateRepository.addWorkmatesSnapshotListener()
        }
    }

    @Subscribe
    fun updateCurrentUserEvent(event: UpdateCurrentUserEvent?) {
        event?.currentUser?.let {
            currentUserLiveData.postValue(event.currentUser)
        }
    }

    @Subscribe
    fun updateWorkmatesEvent(event: UpdateWorkmatesEvent?) {
        event?.workmateList?.let {
            workmatesLiveData.postValue(event.workmateList)
        }
    }

    fun getAllDetailRestaurant() {
        getRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        if (lastKnownLocation.value != null) {
            getRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
                val restaurantsDetail =
                    restaurantRepository.getAllDetailRestaurant(nearRestaurantsLiveData.value)
                restaurantsDetailLiveData.postValue(restaurantsDetail)
            }
        }
    }

    fun getAllNearRestaurant(radius: Int) {

        getNearRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getNearRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            val nearRestaurants =
                restaurantRepository.getAllNearRestaurant(lastKnownLocation.value, radius)
            nearRestaurantsLiveData.postValue(nearRestaurants)
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}