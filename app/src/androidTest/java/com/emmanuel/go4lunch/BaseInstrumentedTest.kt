package com.emmanuel.go4lunch

import android.app.Instrumentation
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import com.emmanuel.go4lunch.di.AppComponentTest
import com.emmanuel.go4lunch.utils.OkHttpProvider
import com.jakewharton.espresso.OkHttp3IdlingResource

open class BaseInstrumentedTest {
    private var instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    lateinit var component: AppComponentTest

    protected fun initialize(){
        IdlingRegistry.getInstance().register(OkHttp3IdlingResource.create("okhttp", OkHttpProvider.instance))

        val app: App = instrumentation.targetContext.applicationContext as App
        component = app.appComponent() as AppComponentTest
    }
}