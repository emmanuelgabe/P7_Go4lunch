package com.emmanuel.go4lunch.di.modules

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

const val GOOGLE_MAP_BASE_URL = "https://maps.googleapis.com"

@Module
open class ApiModule {
    @Singleton
    @Provides
    open fun provideRetrofit(): Retrofit {
        val retrofitBuilder: Retrofit.Builder by lazy {
            Retrofit.Builder()
                .baseUrl(GOOGLE_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
        }
        return retrofitBuilder.build()
    }

    @Singleton
    @Provides
    open fun provideFireStoreService(firestore: FirebaseFirestore): FirestoreService {
        return FirestoreService(firestore)
    }

    @Singleton
    @Provides
    open fun provideGoogleMapService(retrofit: Retrofit): GoogleMapsService {
        return retrofit.create(GoogleMapsService::class.java)
    }
}