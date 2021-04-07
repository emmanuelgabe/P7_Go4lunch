package com.emmanuel.go4lunch.di

import com.emmanuel.go4lunch.di.modules.ApiModule
import com.emmanuel.go4lunch.di.modules.RepositoryModule
import dagger.Component

@Component(modules = [ApiModule::class,RepositoryModule::class])
interface AppComponent {

}