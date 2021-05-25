package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.AuthenticationActivity
import com.emmanuel.go4lunch.MainActivity
import com.emmanuel.go4lunch.di.modules.ApiModule
import com.emmanuel.go4lunch.di.modules.DaoModule
import com.emmanuel.go4lunch.di.modules.FireStoreModule
import com.emmanuel.go4lunch.di.modules.RepositoryModule
import com.emmanuel.go4lunch.ui.restaurantdetail.RestaurantDetailFragment
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import com.emmanuel.go4lunch.ui.workmates.WorkmatesFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApiModule::class, RepositoryModule::class, /*ContextModule::class,*/ DaoModule::class, FireStoreModule::class])
interface AppComponent {
    fun inject(activity: AuthenticationActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: RestaurantDetailFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: WorkmatesFragment)
}