package com.emmanuel.go4lunch.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.emmanuel.go4lunch.data.database.model.RestaurantDetail

@Database(entities = [RestaurantDetail::class], version = 1)
 abstract class RestaurantDetailDatabase() : RoomDatabase() {
    abstract fun restaurantDetailDao(): RestaurantDetailDao

    companion object {
        @Volatile
        private var INSTANCE: RestaurantDetailDatabase? = null

        fun getRestaurantDetailDatabase(context: Context): RestaurantDetailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RestaurantDetailDatabase::class.java,
                    "restaurant_detail_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}