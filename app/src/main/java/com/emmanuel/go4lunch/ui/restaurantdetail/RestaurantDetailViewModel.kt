package com.emmanuel.go4lunch.ui.restaurantdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.model.Restaurant
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.util.*

class RestaurantDetailViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val workmateRepository: WorkmateRepository
) : ViewModel() {
    val currentRestaurantsDetailLiveData = MutableLiveData<NearByRestaurant>()
    val workmatesLiveData = MutableLiveData<List<Workmate>>()
    private val currentWorkmate = MutableLiveData<Workmate>()

    private var getDetailRestaurantJob: Job? = null
    private var getAllWorkmatesJob: Job? = null
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

    private fun getAllWorkmate() {
        getAllWorkmatesJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        getAllWorkmatesJob = viewModelScope.launch(Dispatchers.IO) {
            val workmates = workmateRepository.getAllWorkmate()
            withContext(Dispatchers.Main) {
                workmatesLiveData.value = workmates
                for (workmate in workmates) {
                    if (workmate.uid == FirebaseAuth.getInstance().uid) {
                        currentWorkmate.value = workmate
                    }
                }
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
        getAllWorkmate()
    }

    fun updateLikeRestaurant() {
        updateRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateRestaurant = viewModelScope.launch(Dispatchers.IO) {
            workmateRepository.updateWorkmate(getWorkmateWithUpdateLike())
            getAllWorkmate()
        }
    }

    fun updateFavoriteRestaurant() {
        updateRestaurant?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        updateRestaurant = viewModelScope.launch(Dispatchers.IO) {

            launch {
                val updatedWorkmate = currentWorkmate.value
                if (currentWorkmate.value?.restaurantFavorite.equals(
                        currentRestaurantsDetailLiveData.value?.placeId
                    )
                ) {
                    // Delete favorite restaurant if the current restaurant is already present in favorite
                    updatedWorkmate!!.restaurantFavorite = null
                    updatedWorkmate.favoriteDate = null
                    launch {
                        workmateRepository.updateWorkmate(updatedWorkmate)
                        getAllWorkmate()
                    }.join()
                    // Delete current restaurant information if the current restaurant no longer in favorites for any users
                    if (restaurantIsNeverUse(currentRestaurantsDetailLiveData.value!!.placeId)) {
                        workmateRepository.deleteRestaurant(currentRestaurantsDetailLiveData.value!!.placeId)
                    }
                } else {
                    // Before add restaurant to favorite add in db if it is not yet listed for any users
                    if (restaurantIsNeverUse(currentWorkmate.value?.restaurantFavorite.toString())) {
                        launch {
                            addRestaurant(
                                Restaurant(
                                    currentRestaurantsDetailLiveData.value?.placeId!!,
                                    currentRestaurantsDetailLiveData.value?.name
                                )
                            )
                        }.join()
                    }
                    updatedWorkmate!!.restaurantFavorite =
                        currentRestaurantsDetailLiveData.value?.placeId
                    updatedWorkmate.favoriteDate = Calendar.getInstance().time
                    workmateRepository.updateWorkmate(updatedWorkmate)
                    getAllWorkmate()
                }
            }
        }
    }

    private fun getWorkmateWithUpdateLike(): Workmate {
        val newLikeList = mutableListOf<String>()
        currentWorkmate.value?.restaurantsIdLike?.let {
            newLikeList.addAll(currentWorkmate.value?.restaurantsIdLike!!)
            if (currentWorkmate.value?.restaurantsIdLike!!.contains(
                    currentRestaurantsDetailLiveData.value?.placeId
                )
            ) {
                newLikeList.remove(currentRestaurantsDetailLiveData.value?.placeId.toString())
            } else {
                newLikeList.add(currentRestaurantsDetailLiveData.value?.placeId.toString())
            }
        }
        val updatedWorkmate = currentWorkmate.value
        updatedWorkmate!!.restaurantsIdLike = newLikeList
        return updatedWorkmate
    }

    private fun restaurantIsNeverUse(IdRestaurantToDeleteIfNeverUse: String): Boolean {
        var isNeverUse = true
        for (workmate in workmatesLiveData.value!!) {
            if (workmate.restaurantFavorite.equals(IdRestaurantToDeleteIfNeverUse))
                isNeverUse = false
        }
        return isNeverUse
    }
}