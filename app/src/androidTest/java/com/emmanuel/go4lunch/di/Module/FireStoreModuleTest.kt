package com.emmanuel.go4lunch.di.Module

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FireStoreModuleTest {
    @Singleton
    @Provides
    fun provideFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}