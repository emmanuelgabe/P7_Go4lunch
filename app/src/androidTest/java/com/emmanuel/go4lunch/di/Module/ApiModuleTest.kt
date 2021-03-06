package com.emmanuel.go4lunch.di.Module

import com.emmanuel.go4lunch.data.api.FirestoreService
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import javax.inject.Singleton

@Module
class ApiModuleTest {

/*    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        val retrofitBuilder: Retrofit.Builder by lazy {
            Retrofit.Builder()
                .baseUrl(GOOGLE_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
        }
        return retrofitBuilder.build()
    }*/

    @Singleton
    @Provides
    fun provideFireStoreService(fireStore: FirebaseFirestore): FirestoreService {
        return FirestoreService(fireStore)
    }

    @Singleton
    @Provides
    fun provideGoogleMapService(): GoogleMapsService =
        Mockito.mock(GoogleMapsService::class.java)
}