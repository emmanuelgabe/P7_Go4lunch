package com.emmanuel.go4lunch.ui.restaurantdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.database.model.RestaurantDetail
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import kotlinx.coroutines.*
import java.util.*

class RestaurantDetailViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val workmateRepository: WorkmateRepository
) : ViewModel() {
    val currentRestaurantsDetailLiveData = MutableLiveData<RestaurantDetail>()

    private var getDetailRestaurantJob: Job? = null
    private var updateFavoriteRestaurant: Job? = null
    private var updateLikeRestaurant: Job? = null

    private fun getDetailRestaurant(id: String) {
        getDetailRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getDetailRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            val restaurantDetail =  restaurantRepository.getDetailRestaurant(id)
        currentRestaurantsDetailLiveData.postValue(restaurantDetail)
        }
    }

    private fun addRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            workmateRepository.addRestaurant(restaurant)
        }
    }

    fun init(restaurantId: String) {
        if (currentRestaurantsDetailLiveData.value == null) {
            getDetailRestaurant(restaurantId)
        }
    }

    fun updateLikeRestaurant(currentWorkmate: Workmate) {
        updateLikeRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateLikeRestaurant = viewModelScope.launch(Dispatchers.IO) {
            workmateRepository.updateWorkmate(getWorkmateWithUpdateLike(currentWorkmate))
        }
    }

    fun updateFavoriteRestaurant(currentWorkmate: Workmate, workmates: List<Workmate>) {
        updateFavoriteRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateFavoriteRestaurant = viewModelScope.launch(Dispatchers.IO) {

            if (currentWorkmate.restaurantFavorite != null) {
                // if user has already restaurant in favorite
                if (currentWorkmate.restaurantFavorite.equals(currentRestaurantsDetailLiveData.value?.id)) {
                    // if user favorite restaurant is the current restaurant display
                    currentWorkmate.restaurantFavorite = null
                    currentWorkmate.favoriteDate = null
                    workmateRepository.updateWorkmate(currentWorkmate)
                    // delete old restaurant favorite information if never user from all workmates
                    if (restaurantIsNeverUse(currentRestaurantsDetailLiveData.value!!.id,workmates,currentWorkmate.uid))
                        workmateRepository.deleteRestaurant(currentRestaurantsDetailLiveData.value!!.id)
                } else {
                    // if user favorite restaurant is not the current restaurant display
                    val oldFavoriteId: String? = currentWorkmate.restaurantFavorite
                    currentWorkmate.restaurantFavorite = currentRestaurantsDetailLiveData.value?.id
                    currentWorkmate.favoriteDate = Calendar.getInstance().time
                    workmateRepository.updateWorkmate(currentWorkmate)
                    // delete old restaurant favorite information if never user from all workmates
                    if (restaurantIsNeverUse(oldFavoriteId!!, workmates, currentWorkmate.uid))
                        workmateRepository.deleteRestaurant(oldFavoriteId)
                    // add new restaurant favorite information if not already add by an other user
                    if (restaurantIsNeverUse(currentRestaurantsDetailLiveData.value!!.id, workmates, currentWorkmate.uid))
                        addRestaurant(Restaurant(currentRestaurantsDetailLiveData.value?.id!!, currentRestaurantsDetailLiveData.value?.name))
                }
            } else { // if user has no restaurant in favorite
                // check if favorite information has already add by an other user
                if (restaurantIsNeverUse(currentRestaurantsDetailLiveData.value!!.id, workmates, currentWorkmate.uid))
                    addRestaurant(Restaurant(currentRestaurantsDetailLiveData.value?.id!!, currentRestaurantsDetailLiveData.value?.name))
                currentWorkmate.restaurantFavorite = currentRestaurantsDetailLiveData.value?.id
                currentWorkmate.favoriteDate = Calendar.getInstance().time
                workmateRepository.updateWorkmate(currentWorkmate)
                }
            }
        }
    
    private fun getWorkmateWithUpdateLike(currentWorkmate: Workmate): Workmate {
        val newLikeList = mutableListOf<String>()
        currentWorkmate.restaurantsIdLike?.let {
            newLikeList.addAll(currentWorkmate.restaurantsIdLike!!)
            if (currentWorkmate.restaurantsIdLike!!.contains(
                    currentRestaurantsDetailLiveData.value?.id
                )
            ) {
                newLikeList.remove(currentRestaurantsDetailLiveData.value?.id.toString())
            } else {
                newLikeList.add(currentRestaurantsDetailLiveData.value?.id.toString())
            }
        }
        currentWorkmate.restaurantsIdLike = newLikeList
        return currentWorkmate
    }

    private fun restaurantIsNeverUse(restaurantId: String, workmates: List<Workmate>, currentUserId: String): Boolean {
        var isNeverUse = true
        for (workmate in workmates) {
            if (workmate.uid != currentUserId && workmate.restaurantFavorite.equals(restaurantId))
                isNeverUse = false
        }
        return isNeverUse
    }
}