package com.emmanuel.go4lunch.ui.restaurantdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
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
    val currentRestaurantsDetailLiveData = MutableLiveData<NearByRestaurant>()

    private var getDetailRestaurantJob: Job? = null
    private var updateRestaurant: Job? = null


    private fun getDetailRestaurant(id: String?, currentRestaurantDetail: NearByRestaurant?) {
        getDetailRestaurantJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getDetailRestaurantJob = viewModelScope.launch(Dispatchers.IO) {
            // If detail is showing from listView fragment detail has already been fetch
            // thus no need to download the information by the id
            val restaurantDetail: NearByRestaurant? = if (id != null) {
                restaurantRepository.getDetailRestaurant(id)
            } else {
                currentRestaurantDetail
            }
            withContext(Dispatchers.Main) {
                currentRestaurantsDetailLiveData.value = restaurantDetail
            }
        }
    }

    private fun addRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            workmateRepository.addRestaurant(restaurant)
        }
    }

    fun init(restaurantId: String?, currentRestaurantDetail: NearByRestaurant?) {
        if (currentRestaurantsDetailLiveData.value == null) {
            getDetailRestaurant(restaurantId, currentRestaurantDetail)
        }
    }

    fun updateLikeRestaurant(currentWorkmate: Workmate) {
        updateRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateRestaurant = viewModelScope.launch(Dispatchers.IO) {
            workmateRepository.updateWorkmate(getWorkmateWithUpdateLike(currentWorkmate))
        }
    }

    fun updateFavoriteRestaurant(currentWorkmate: Workmate, workmates: List<Workmate>) {
        updateRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateRestaurant = viewModelScope.launch(Dispatchers.IO) {

            launch {
                if (currentWorkmate.restaurantFavorite.equals(
                        currentRestaurantsDetailLiveData.value?.placeId
                    )
                ) {
                    // Delete favorite restaurant if the current restaurant is already present in favorite
                    currentWorkmate.restaurantFavorite = null
                    currentWorkmate.favoriteDate = null
                    launch {
                        workmateRepository.updateWorkmate(currentWorkmate)
                    }.join()
                    // Delete current restaurant information if the current restaurant no longer in favorites for any users
                    if (restaurantIsNeverUse(currentRestaurantsDetailLiveData.value!!.placeId,workmates)) {
                        workmateRepository.deleteRestaurant(currentRestaurantsDetailLiveData.value!!.placeId)
                    }
                } else {
                    // Before add restaurant to favorite add in db if it is not yet listed for any users
                    if (restaurantIsNeverUse(currentWorkmate.restaurantFavorite.toString(),workmates)) {
                        launch {
                            addRestaurant(
                                Restaurant(
                                    currentRestaurantsDetailLiveData.value?.placeId!!,
                                    currentRestaurantsDetailLiveData.value?.name
                                )
                            )
                        }.join()
                    }
                    currentWorkmate.restaurantFavorite =
                        currentRestaurantsDetailLiveData.value?.placeId
                    currentWorkmate.favoriteDate = Calendar.getInstance().time
                    workmateRepository.updateWorkmate(currentWorkmate)
                }
            }
        }
    }

    private fun getWorkmateWithUpdateLike(currentWorkmate: Workmate): Workmate {
        val newLikeList = mutableListOf<String>()
        currentWorkmate.restaurantsIdLike?.let {
            newLikeList.addAll(currentWorkmate.restaurantsIdLike!!)
            if (currentWorkmate.restaurantsIdLike!!.contains(
                    currentRestaurantsDetailLiveData.value?.placeId
                )
            ) {
                newLikeList.remove(currentRestaurantsDetailLiveData.value?.placeId.toString())
            } else {
                newLikeList.add(currentRestaurantsDetailLiveData.value?.placeId.toString())
            }
        }
        currentWorkmate.restaurantsIdLike = newLikeList
        return currentWorkmate
    }

    private fun restaurantIsNeverUse(
        IdRestaurantToDeleteIfNeverUse: String,
        workmates: List<Workmate>
    ): Boolean {
        var isNeverUse = true
        for (workmate in workmates) {
            if (workmate.restaurantFavorite.equals(IdRestaurantToDeleteIfNeverUse))
                isNeverUse = false
        }
        return isNeverUse
    }
}