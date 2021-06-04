package com.emmanuel.go4lunch.di.modules

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class FireStoreModule {

    @Singleton
    @Provides
    open fun provideFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()

}