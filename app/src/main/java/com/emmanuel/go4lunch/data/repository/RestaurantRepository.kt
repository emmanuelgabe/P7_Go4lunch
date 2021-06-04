package com.emmanuel.go4lunch.data.repository

import android.location.Location
import com.emmanuel.go4lunch.BuildConfig
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.emmanuel.go4lunch.data.api.model.NearByRestaurant
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import com.emmanuel.go4lunch.data.database.model.RestaurantDetailEntity

open class RestaurantRepository(
    private val googleMapService: GoogleMapsService,
    private val restaurantDetailDao: RestaurantDetailDao
) {

    suspend fun getAllNearRestaurant(
        lastKnownLocation: Location?,
        radius: Int
    ): List<NearByRestaurant> {
        val response = googleMapService.getNearRestaurant(
            "${lastKnownLocation?.latitude},${lastKnownLocation?.longitude}", radius,
            "restaurant",
            BuildConfig.GOOGLE_MAP_API_KEY,
        )

        while ((response.results.size < MAX_NEAR_RESTAURANT && !response.nextPageToken.isNullOrBlank())){
            val nextPageResponse =  googleMapService.getNearRestaurantNextPage("${lastKnownLocation?.latitude},${lastKnownLocation?.longitude}", radius,
                "restaurant",
                BuildConfig.GOOGLE_MAP_API_KEY,
                response.nextPageToken!!
            )
            response.nextPageToken.equals(nextPageResponse.nextPageToken)
            for (restaurants in nextPageResponse.results){
                response.results.add(restaurants)
            }
        }
        return response.results
    }

    private suspend fun getDetailRestaurantFromGoogleApi(restaurantsId: String): NearByRestaurant? {
        val fields = listOf(
            "place_id", "name", "business_status", "rating", "user_ratings_total",
            "vicinity", "formatted_phone_number", "price_level", "geometry",
            "photo", "opening_hours", "formatted_phone_number", "website"
        )
        val response = googleMapService.getDetails(
            restaurantsId,
            fields.joinToString(separator = ","),
            BuildConfig.GOOGLE_MAP_API_KEY
        )
        return response.result
    }

    /**
     * Fetch restaurant detail from local room database, if it is not present download the restaurant details from google api to save it in the database.
     * If the restaurant was saved more than two days ago. its data will be updated in the database
     */
    suspend fun getDetailRestaurant(id: String): RestaurantDetailEntity {
        if (!restaurantDetailDao.restaurantExists(id)) {
            val newRestaurant = getDetailRestaurantFromGoogleApi(id)!!
            restaurantDetailDao.insertRestaurantDetail(
                instanceRestaurantDetailFromNearByRestaurant(newRestaurant)
            )
        } else {
            if ((System.currentTimeMillis() - restaurantDetailDao.getRestaurantDetailsTimestamp(id)) > TIME_BEFORE_UPDATE) {
                val updatedRestaurant = getDetailRestaurantFromGoogleApi(id)!!
                restaurantDetailDao.updateRestaurantDetail(
                    instanceRestaurantDetailFromNearByRestaurant(updatedRestaurant)
                )
            }
        }
        return restaurantDetailDao.getRestaurantDetailsById(id)
    }

    suspend fun getAllDetailRestaurant(nearRestaurants: List<NearByRestaurant>?): List<RestaurantDetailEntity> {
        val restaurantsDetailList = mutableListOf<RestaurantDetailEntity>()
        nearRestaurants?.let {
            for (restaurant in nearRestaurants) {
                restaurantsDetailList.add(getDetailRestaurant(restaurant.placeId))
            }
        }
        return restaurantsDetailList
    }

    private fun instanceRestaurantDetailFromNearByRestaurant(restaurant: NearByRestaurant): RestaurantDetailEntity {
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

    companion object {
        const val TIME_BEFORE_UPDATE = 172800000 // 2 days in ms
        const val MAX_NEAR_RESTAURANT = 60 // must be a multiple of 20
    }
}