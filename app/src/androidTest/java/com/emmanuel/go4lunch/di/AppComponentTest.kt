package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.di.Module.ApiModuleTest
import com.emmanuel.go4lunch.di.Module.DaoModuleTest
import com.emmanuel.go4lunch.di.Module.FireStoreModuleTest
import com.emmanuel.go4lunch.di.Module.RepositoryModuleTest
import com.emmanuel.go4lunch.MainActivityTestDagger
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApiModuleTest::class, RepositoryModuleTest::class, /*ContextModule::class,*/ DaoModuleTest::class, FireStoreModuleTest::class])
    interface AppComponentTest: AppComponent {
    fun inject(activity: MainActivityTestDagger)
}