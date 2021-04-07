package com.emmanuel.go4lunch

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.emmanuel.go4lunch.data.model.Workmate
import com.emmanuel.go4lunch.utils.UpdateCurrentUserEvent
import org.greenrobot.eventbus.EventBus
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityInitialisation_viewsCorrectlyDisplay() {
        onView(withId(R.id.toolbar_open_search_button)).check(matches(isDisplayed()))
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar_clear_search_button)).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
        onView(withId(R.id.search_auto_complete_text_view)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )
        onView(withId(R.id.toolbar_valid_search_button)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav_view)).check(matches(isDisplayed()))
               onView(withId(R.id.map)).check(matches(isDisplayed()))
             onView(withId(R.id.fragment_map_view_gps_fab)).check(matches(isDisplayed()))
    }

    @Test
    fun navigation_fragmentCorrectlyDisplay() {
        onView(withId(R.id.fragment_map_view_gps_fab)).check(matches(isDisplayed()))
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.listViewFragment), withContentDescription("list view"),
                childAtPosition(childAtPosition(withId(R.id.bottom_nav_view), 0), 1)
            )
        )
            .perform(click())
        onView(
            allOf(
                withId(R.id.fragment_list_view_progress_bar),
                withParent(withParent(withId(R.id.nav_host_fragment)))
            )
        )
            .check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.workmatesFragment), withContentDescription("workmates"),
                childAtPosition(childAtPosition(withId(R.id.bottom_nav_view), 0), 2)
            )
        )
            .perform(click())
        onView(
            allOf(
                withId(R.id.workmates_recycler_view),
                withParent(
                    allOf(
                        withId(R.id.nav_host_fragment),
                        withParent(withId(R.id.nav_host_fragment))
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.mapViewFragment),
                withContentDescription("map view"),
                childAtPosition(childAtPosition(withId(R.id.bottom_nav_view), 0), 0)
            )
        )
            .perform(click())
        onView(
            allOf(
                withContentDescription("Google Map"),
                withParent(withParent(withId(R.id.map)))
            )
        )
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseSearchBar_SearchBarCorrectlyDisplay() {
        // open search bar
        onView(allOf(withId(R.id.toolbar_open_search_button), isDisplayed())).perform(click())
        // check search bar is display
        onView(allOf(withId(R.id.search_auto_complete_text_view)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.toolbar_valid_search_button)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.toolbar_clear_search_button)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.toolbar_open_search_button)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        // close search bar
       onView(allOf(withId(R.id.toolbar_clear_search_button))).perform(click())
        // check search correctly close
        onView(allOf(withId(R.id.search_auto_complete_text_view)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(allOf(withId(R.id.toolbar_valid_search_button)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(allOf(withId(R.id.toolbar_clear_search_button)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(allOf(withId(R.id.toolbar_open_search_button)))
            .check(matches(isDisplayed()))
    }
    @Test
    fun navigationDrawer_correctInteraction() {

        // mock current user information with eventBus
        EventBus.getDefault().post(UpdateCurrentUserEvent(Workmate("uid123456789Test",
            "emaildetest@test.fr","my name",
            "https://www.nretnil.com/avatar/LawrenceEzekielAmos.png")))

        // Open navigation drawer
        onView(
            allOf(
                withContentDescription("Open navigation drawer"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(withId(R.id.container_main), 0)
                    ), 2
                )
            )
        ).perform(click())

        // check drawer is correctly display :
        onView(
            allOf(
                withId(R.id.drawer_header_username_text_view), withParent(
                    allOf(
                        withId(
                            R.id.header
                        )
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.drawer_header_user_image), withParent(
                    allOf(
                        withId(
                            R.id.header
                        ),
                        withParent(withId(R.id.navigation_header_container))
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.drawer_header_user_email_text_view),
                withParent(
                    allOf(
                        withId(R.id.header),
                        withParent(withId(R.id.navigation_header_container))
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.yourLunch), childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(withId(R.id.drawer_nav_view), 0)
                    ), 1
                )
            )
        )
            .perform(click())

        onView(
            allOf(
                withId(R.id.logout_item),
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.drawer_nav_view),
                            0
                        )
                    ),
                    3
                )
            )
        ).perform(click())
        assertEquals(activityRule.scenario.state, Lifecycle.State.DESTROYED)
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