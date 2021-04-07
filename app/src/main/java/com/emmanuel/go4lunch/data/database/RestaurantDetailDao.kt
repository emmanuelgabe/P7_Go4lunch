package com.emmanuel.go4lunch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emmanuel.go4lunch.data.database.model.RestaurantDetailEntity


@Dao
interface RestaurantDetailDao {
    @Query("SELECT * FROM RestaurantDetailEntity")
    fun getAllRestaurantsDetails(): List<RestaurantDetailEntity>

    @Query("SELECT * FROM RestaurantDetailEntity  WHERE id = :restaurantId")
    fun getRestaurantDetailsById(restaurantId: String): RestaurantDetailEntity

    @Query("SELECT creation_time_stamp FROM RestaurantDetailEntity  WHERE id = :restaurantId")
    fun getRestaurantDetailsTimestamp(restaurantId: String): Long

    @Query("SELECT EXISTS (SELECT 1 FROM RestaurantDetailEntity WHERE id = :restaurantId)")
    fun restaurantExists(restaurantId: String): Boolean

    @Insert
    fun insertRestaurantDetail(restaurantDetail: RestaurantDetailEntity)

    @Update
    fun updateRestaurantDetail(restaurantDetail: RestaurantDetailEntity)
}