package com.emmanuel.go4lunch

import android.location.Location
import com.emmanuel.go4lunch.data.repository.PlaceRepository
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainViewModelTest{
    private lateinit var workmateRepository: WorkmateRepository
    private lateinit var placeRepository: PlaceRepository
    private lateinit var restaurantRepository: RestaurantRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fakeLocation: Location

    @Before
    fun setUp() {
        workmateRepository = Mockito.mock(WorkmateRepository::class.java)
        placeRepository = Mockito.mock(PlaceRepository::class.java)
        restaurantRepository = Mockito.mock(RestaurantRepository::class.java)
        fakeLocation = Mockito.mock(Location::class.java)
        mainViewModel = MainViewModel(workmateRepository,placeRepository,restaurantRepository)
    }

    @Test
    fun mainViewModelTest() = runBlocking {
        var lastKnownLocationValue = mainViewModel.lastKnownLocation.value
        assert(lastKnownLocationValue == null)
        mainViewModel.saveLocation(fakeLocation)
        lastKnownLocationValue = mainViewModel.lastKnownLocation.value
        assertTrue(lastKnownLocationValue == fakeLocation)
    }
}