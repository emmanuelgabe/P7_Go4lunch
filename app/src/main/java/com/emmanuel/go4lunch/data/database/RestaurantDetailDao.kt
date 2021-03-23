package com.emmanuel.go4lunch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emmanuel.go4lunch.data.database.model.RestaurantDetail


@Dao
interface RestaurantDetailDao {
    @Query("SELECT * FROM restaurantDetail")
    fun getAllRestaurantsDetails(): List<RestaurantDetail>

    @Query("SELECT * FROM restaurantDetail  WHERE id = :restaurantId")
    fun getRestaurantDetailsById(restaurantId: String): RestaurantDetail

    @Query("SELECT creation_time_stamp FROM restaurantDetail  WHERE id = :restaurantId")
    fun getRestaurantDetailsTimestamp(restaurantId: String): Long

    @Query("SELECT EXISTS (SELECT 1 FROM restaurantDetail WHERE id = :restaurantId)")
    fun restaurantExists(restaurantId: String): Boolean

    @Insert
    fun insertRestaurantDetail(restaurantDetail: RestaurantDetail)

    @Update
    fun updateRestaurantDetail(restaurantDetail: RestaurantDetail)
}