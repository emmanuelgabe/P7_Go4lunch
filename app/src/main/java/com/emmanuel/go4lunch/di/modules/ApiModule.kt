package com.emmanuel.go4lunch.di.modules

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class ApiModule {
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        val retrofitBuilder: Retrofit.Builder by lazy {
            Retrofit.Builder()
                .baseUrl(GOOGLE_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
        }
        return retrofitBuilder.build()
    }

    @Singleton
    @Provides
    fun provideFireStoreService(fireStore: FirebaseFirestore): FirestoreService {
        return FirestoreService(fireStore)
    }

    @Singleton
    @Provides
    fun provideGoogleMapService(retrofit: Retrofit): GoogleMapsService {
        return retrofit.create(GoogleMapsService::class.java)
    }

    companion object {
        const val GOOGLE_MAP_BASE_URL = "https://maps.googleapis.com"
    }
}