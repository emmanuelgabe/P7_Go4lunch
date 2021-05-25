/*
package com.emmanuel.go4lunch

import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.data.repository.WorkmateRepository
import com.emmanuel.go4lunch.ui.restaurantdetail.RestaurantDetailViewModel
import io.mockk.mockk
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RestaurantDetailEntityViewModelTest {

    lateinit var restaurantDetailViewModel: RestaurantDetailViewModel

    @Before
    fun setUp(){
        val restaurantRepository = mockk<RestaurantRepository>()
        val workmateRepository = mockk<WorkmateRepository>()
        restaurantDetailViewModel = RestaurantDetailViewModel(restaurantRepository,workmateRepository)
    }
    @Test
    fun restaurantIsNeverUse_currentRestaurantIdNotUse_ReturnTrue() {
        val restaurantIsNeveruUse = restaurantDetailViewModel.restaurantIsNeverUse(
            "currentRestaurantId", getFakeWorkmateList(), "currentUserId"
        )
        assertTrue(restaurantIsNeveruUse)
    }

    @Test
    fun restaurantIsNeverUse_currentRestaurantIdByCurrentUser_ReturnTrue() {
        val restaurantIsNeveruUse = restaurantDetailViewModel.restaurantIsNeverUse(
            "restaurantId1", getFakeWorkmateList(), "fakeUserUid1"
        )
        assertTrue(restaurantIsNeveruUse)
    }

    @Test
    fun restaurantIsNeverUse_currentRestaurantIdByWorkmate_ReturnFalse() {
        val restaurantIsNeveruUse = restaurantDetailViewModel.restaurantIsNeverUse(
            "restaurantId1", getFakeWorkmateList(), "currentUserId"
        )
        assertFalse(restaurantIsNeveruUse)
    }


    fun getFakeWorkmateList(): List<Workmate> {
        return listOf(
            Workmate(
                "fakeUserUid1",
                null,
                null,
                null,
                restaurantFavorite = "restaurantId1"
            ), Workmate(
                "fakeUserUid2",
                null,
                null,
                null,
                restaurantFavorite = "restaurantId2"
            ), Workmate(
                "fakeUserUid3",
                null,
                null,
                null,
                restaurantFavorite = "restaurantId3"
            )
        )
    }
}*/
