package com.emmanuel.go4lunch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val workmateRepository: WorkmateRepository) : ViewModel() {
    val workmatesLiveData = MutableLiveData<Workmate>()

    fun getUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workmate = workmateRepository.getUser(userId)
            withContext(Dispatchers.Main) {
            workmatesLiveData.value = workmate
            }
        }
    }
}