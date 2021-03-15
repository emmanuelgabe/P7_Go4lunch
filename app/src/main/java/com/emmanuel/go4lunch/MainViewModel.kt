package com.emmanuel.go4lunch

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.api.response.Prediction
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class MainViewModel(
    private val workmateRepository: WorkmateRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {
    private var _location: MutableLiveData<Location> = MutableLiveData<Location>()
    val location: LiveData<Location> = _location
    val placesAutocompleteLiveData = MutableLiveData<List<Prediction>>()

    val searchInput = MutableLiveData<String>()
    val workmatesLiveData = MutableLiveData<List<Workmate>>()
    val currentUserLiveData = MutableLiveData<Workmate>()

    private var placeJob: Job? = null
    private var getWorkmateJob: Job? = null

    fun saveLocation(location: Location?) {
        _location.value = location
    }

    fun setInput(input: String){
        searchInput.value = input
    }

    fun getPlaces(input: String) {
        placeJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        placeJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            val placeResponse = placeRepository.getPlaces(input, location.value!!)
            withContext(Dispatchers.Main) {
                if (placeResponse != null) {
                    withContext(Dispatchers.Main) {
                    placesAutocompleteLiveData.value = placeResponse.predictions
                }}
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
            workmateRepository.getAllWorkmate().addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null){
                    val workmateList = mutableListOf<Workmate>()
                    for (document in snapshot.documents) {
                        @Suppress("UNCHECKED_CAST")
                        workmateList.add(
                            Workmate(
                                document.id,
                                document.get("email").toString(),
                                document.get("name").toString(),
                                document.get("avatarURL").toString(),
                                document.get("restaurantsLike") as? List<String>?,
                                document.get("restaurantFavorite")?.toString(),
                                document.getTimestamp("favoriteDate")?.toDate()
                            )
                        )
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        for (workmate in workmateList){
                            if(workmate.uid ==  FirebaseAuth.getInstance().uid){
                                currentUserLiveData.value = workmate
                            }
                        workmatesLiveData.value = workmateList
                        }
                    }
                }
            }
        }
    }
}