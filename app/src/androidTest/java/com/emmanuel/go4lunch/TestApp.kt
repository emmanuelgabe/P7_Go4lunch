package com.emmanuel.go4lunch

import com.emmanuel.go4lunch.di.DaggerAppComponentTest
import com.emmanuel.go4lunch.di.Module.DaoModuleTest
import com.emmanuel.go4lunch.di.AppComponent

class TestApp : App() {
    override fun initDagger(): AppComponent {
        return DaggerAppComponentTest.builder()
            // .contextModule(ContextModule(applicationContext))
            .daoModuleTest(DaoModuleTest(applicationContext))
            .build()
    }
}