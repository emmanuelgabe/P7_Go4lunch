package com.emmanuel.go4lunch

import android.location.Location
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import com.emmanuel.go4lunch.data.database.model.RestaurantDetailEntity
import com.emmanuel.go4lunch.data.repository.RestaurantRepository
import com.emmanuel.go4lunch.utils.FakeDataProviderUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.*


@ExperimentalCoroutinesApi
class ApiServiceTest {

    private lateinit var googleMapsService: GoogleMapsService
    private lateinit var restaurantDetailDao: RestaurantDetailDao
    private lateinit var fakeLocation: Location

    @Before
    fun setUp() {
        googleMapsService = Mockito.mock(GoogleMapsService::class.java)
        restaurantDetailDao = Mockito.mock(RestaurantDetailDao::class.java)
        fakeLocation = mock(Location::class.java)
    }

    @Test
    fun getNearRestaurant_restaurantListReturnFromService() = runBlocking {

        `when`(fakeLocation.latitude).thenReturn(0.0)
        `when`(fakeLocation.longitude).thenReturn(0.0)

        `when`(googleMapsService.getNearRestaurant("0.0,0.0",10,"restaurant",BuildConfig.GOOGLE_MAP_API_KEY))
            .thenReturn(FakeDataProviderUnitTest.nearRestaurantList)

        val restaurantRepository = RestaurantRepository(googleMapsService, restaurantDetailDao)
        val nearRestaurant = restaurantRepository.getAllNearRestaurant(fakeLocation, 10)

        verify(googleMapsService,times(1)).getNearRestaurant(anyString(), anyInt(), anyString(), anyString())
        verify(googleMapsService, never()).getPlaces(anyString(),anyString(),anyString(),anyString(),anyInt(),anyString())
        verify(googleMapsService, never()).getDetails( anyString(),anyString(),anyString())

        Assert.assertEquals(16, nearRestaurant?.size)
    }

    @Test
    fun getDetailRestaurant_detailHaveNeverBeenDownload_detailReturnFromServiceAndInsertInLocalDB() = runBlocking {
        `when`(googleMapsService.getDetails(anyString(), anyString(), anyString()))
            .thenReturn(FakeDataProviderUnitTest.nearRestaurantDetail)

        given(restaurantDetailDao.restaurantExists(FAKE_RESTAURANT_ID)).willReturn(false)
        given(restaurantDetailDao.getRestaurantDetailsById(FAKE_RESTAURANT_ID)).willReturn(
            getRestaurantDetailFromNearByRestaurant(FakeDataProviderUnitTest.nearRestaurantDetail.result!!))

        val restaurantRepository = RestaurantRepository(googleMapsService, restaurantDetailDao)
        val restaurantDetail = restaurantRepository.getDetailRestaurant(FAKE_RESTAURANT_ID)

        val inOrder = inOrder(restaurantDetailDao, googleMapsService)
        inOrder.verify(restaurantDetailDao,times(1)).restaurantExists(FAKE_RESTAURANT_ID)
        inOrder.verify(googleMapsService,times(1)).getDetails(anyString(), anyString(), anyString())
        inOrder.verify(restaurantDetailDao,times(1)).insertRestaurantDetail(any(RestaurantDetailEntity::class.java))
        inOrder.verify(restaurantDetailDao,times(1)).getRestaurantDetailsById(FAKE_RESTAURANT_ID)

        verify(googleMapsService, never()).getPlaces(anyString(),anyString(),anyString(),anyString(),anyInt(),anyString())
        verify(googleMapsService, never()).getNearRestaurant( anyString(),anyInt(),anyString(),anyString())

        assert(restaurantDetail.id.equals(FAKE_RESTAURANT_ID))
        assert(restaurantDetail.name.equals("L'Ambroisie"))
    }

    @Test
    fun getDetailInRepository_detailOutdatedInDB_updatedDetailReturnFromServiceAndInsertInLocalDB() = runBlocking {
        `when`(googleMapsService.getDetails(anyString(), anyString(), anyString()))
            .thenReturn(FakeDataProviderUnitTest.nearRestaurantDetail)

        given(restaurantDetailDao.restaurantExists(FAKE_RESTAURANT_ID)).willReturn(true)
        given(restaurantDetailDao.getRestaurantDetailsById(FAKE_RESTAURANT_ID)).willReturn(
            getRestaurantDetailFromNearByRestaurant(FakeDataProviderUnitTest.nearRestaurantDetail.result!!))

        val restaurantRepository = RestaurantRepository(googleMapsService, restaurantDetailDao)
        val restaurantDetail = restaurantRepository.getDetailRestaurant(FAKE_RESTAURANT_ID)

        val inOrder = inOrder(restaurantDetailDao, googleMapsService)
        inOrder.verify(restaurantDetailDao,times(1)).restaurantExists(FAKE_RESTAURANT_ID)
        inOrder.verify(restaurantDetailDao,times(1)).getRestaurantDetailsTimestamp(FAKE_RESTAURANT_ID)
        inOrder.verify(googleMapsService,times(1)).getDetails(anyString(), anyString(), anyString())
        inOrder.verify(restaurantDetailDao,times(1)).updateRestaurantDetail(any(RestaurantDetailEntity::class.java))
        inOrder.verify(restaurantDetailDao,times(1)).getRestaurantDetailsById(FAKE_RESTAURANT_ID)

        verify(googleMapsService, never()).getPlaces(anyString(),anyString(),anyString(),anyString(),anyInt(),anyString())
        verify(googleMapsService, never()).getNearRestaurant( anyString(),anyInt(),anyString(),anyString())

        assert(restaurantDetail.id.equals(FAKE_RESTAURANT_ID))
        assert(restaurantDetail.name.equals("L'Ambroisie"))
    }


    @Test
    fun getDetailInRepository_detailNotOutdatedInDB_updatedDetailReturnFromServiceAndInsertInLocalDB() = runBlocking {
        `when`(googleMapsService.getDetails(anyString(), anyString(), anyString()))
            .thenReturn(FakeDataProviderUnitTest.nearRestaurantDetail)

        given(restaurantDetailDao.restaurantExists(FAKE_RESTAURANT_ID)).willReturn(true)
        given(restaurantDetailDao.getRestaurantDetailsById(FAKE_RESTAURANT_ID)).willReturn(
            getRestaurantDetailFromNearByRestaurant(FakeDataProviderUnitTest.nearRestaurantDetail.result!!))
        given(restaurantDetailDao.restaurantExists(FAKE_RESTAURANT_ID)).willReturn(true)
        given(restaurantDetailDao.getRestaurantDetailsTimestamp(FAKE_RESTAURANT_ID)).willReturn(System.currentTimeMillis() - 1)

        val restaurantRepository = RestaurantRepository(googleMapsService, restaurantDetailDao)
        val restaurantDetail = restaurantRepository.getDetailRestaurant(FAKE_RESTAURANT_ID)

        val inOrder = inOrder(restaurantDetailDao, googleMapsService)
        inOrder.verify(restaurantDetailDao,times(1)).restaurantExists(FAKE_RESTAURANT_ID)
        inOrder.verify(restaurantDetailDao,times(1)).getRestaurantDetailsTimestamp(FAKE_RESTAURANT_ID)
        inOrder.verify(restaurantDetailDao,times(1)).getRestaurantDetailsById(FAKE_RESTAURANT_ID)

        verify(googleMapsService, never()).getPlaces(anyString(),anyString(),anyString(),anyString(),anyInt(),anyString())
        verify(googleMapsService, never()).getNearRestaurant( anyString(),anyInt(),anyString(),anyString())

        assert(restaurantDetail.id.equals(FAKE_RESTAURANT_ID))
        assert(restaurantDetail.name.equals("L'Ambroisie"))
    }

    companion object{
        val FAKE_RESTAURANT_ID = "ChIJK-TZ1v9x5kcRvQoSvNJ21Vo"
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun getRestaurantDetailFromNearByRestaurant(restaurant: NearByRestaurant): RestaurantDetailEntity {
        return RestaurantDetailEntity(
            id = restaurant.placeId,
            name = restaurant.name,
            businessStatus = restaurant.businessStatus,
            rating = restaurant.rating,
            ratingNumber = restaurant.ratingNumber,
            address = restaurant.address,
            price = restaurant.price,
            photoReference = restaurant.photos?.get(0)?.photoReference,
            lat = restaurant.geometry.location.lat,
            lng = restaurant.geometry.location.lng,
            phoneNumber = restaurant.phoneNumber,
            openNow = restaurant.openingHours?.openNow,
            creationTimestamp = System.currentTimeMillis(),
            website = restaurant.website,
            weekdayText1 = restaurant.openingHours?.weekdayText?.get(0),
            weekdayText2 = restaurant.openingHours?.weekdayText?.get(1),
            weekdayText3 = restaurant.openingHours?.weekdayText?.get(2),
            weekdayText4 = restaurant.openingHours?.weekdayText?.get(3),
            weekdayText5 = restaurant.openingHours?.weekdayText?.get(4),
            weekdayText6 = restaurant.openingHours?.weekdayText?.get(5),
            weekdayText7 = restaurant.openingHours?.weekdayText?.get(6),
        )
    }
}