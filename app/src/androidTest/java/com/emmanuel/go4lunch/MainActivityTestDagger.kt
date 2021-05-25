package com.emmanuel.go4lunch

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emmanuel.go4lunch.data.api.GoogleMapsService
import com.emmanuel.go4lunch.data.database.RestaurantDetailDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.inject.Inject


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MainActivityTestDagger : BaseInstrumentedTest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var googleMapsService: GoogleMapsService

    @Inject
    lateinit var restaurantDetailDao: RestaurantDetailDao

    @Before
    fun setUp() {
        initialize()
        component.inject(this)

    }

    @Test
    fun testUi() = runBlockingTest {

        val context: Context = mock(Context::class.java)
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = Location("providerName")
        location.latitude = 37.4219983
        location.longitude = -122.084000
        location.time = System.currentTimeMillis()
        lm.setTestProviderLocation("providerName", location)

        Mockito.`when`(googleMapsService.getNearRestaurant("", 0, "", ""))
            .thenReturn(FakeDataProvider.nearRestaurantList)
        Mockito.`when`(googleMapsService.getDetails("", "", ""))
            .thenReturn(FakeDataProvider.nearRestaurantDetail)

        //  onView(withId(R.id.map)).check(matches(isDisplayed()))

        val bottomNavigationItemView = onView(
            Matchers.allOf(
                withId(R.id.listViewFragment), withContentDescription("list view"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_nav_view),
                        0
                    ), 1
                ), isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        onView(withId(R.id.list_view_recycler_view)).check(ViewAssertions.matches(isDisplayed()))
   //   onView(withId(R.id.list_view_recycler_view)).check(RecyclerViewUtils.ItemCount(1))
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}