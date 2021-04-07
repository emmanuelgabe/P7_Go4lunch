package com.emmanuel.go4lunch

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.emmanuel.go4lunch.ui.settings.SettingsFragment
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SettingFragmentTest {

    private lateinit var sharedPreferences: SharedPreferences

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun initSharedPreferences() {
        val context: Context =  getApplicationContext()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
    @Test
    fun useSettingFragmentInterface_InterfaceShowCorrectView(){
        // open setting fragment
        onView(ViewMatchers.withContentDescription("Open navigation drawer")).perform(
            ViewActions.click()
        )
        onView(ViewMatchers.withId(R.id.settingsFragment)).perform(ViewActions.click())
        // open delete account dialog
        onView(ViewMatchers.withText("Delete Account")).perform(ViewActions.click())
        // close delete account dialog
        onView(Matchers.allOf(ViewMatchers.withId(android.R.id.button2), ViewMatchers.withText("Cancel")))
            .perform(ViewActions.scrollTo(), ViewActions.click())
        // check default value google map zoom
        onView(Matchers.allOf(ViewMatchers.withId(R.id.seekbar_value), ViewMatchers.withText("15"))
        ).check(ViewAssertions.matches(ViewMatchers.withText("15")))
        // check default value radius
        onView(Matchers.allOf(ViewMatchers.withId(R.id.seekbar_value), ViewMatchers.withText("1000"))
        ).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
    }
    @Test
    fun changeNotificationSettings_settingsAreSavedInTheApplication() {
        // open setting fragment
        onView(ViewMatchers.withContentDescription("Open navigation drawer")).perform(
            ViewActions.click()
        )
        onView(ViewMatchers.withId(R.id.settingsFragment)).perform(ViewActions.click())
        var notificationPreference = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATION_PREFERENCE,false)
        var notificationHour = sharedPreferences.getString(SettingsFragment.KEY_PREF_NOTIFICATION_HOUR_PREFERENCE,"12")!!
            .toInt()
        // check devauld value befor change
        assertEquals(notificationPreference, true)
        assertEquals(notificationHour, 11)
        // open hour notification dialog
        onView(ViewMatchers.withText("Notification display hour")).perform(ViewActions.click())
        // select hour
        onView(ViewMatchers.withText("11")).perform(ViewActions.click())

         notificationPreference = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATION_PREFERENCE,false)
         notificationHour = sharedPreferences.getString(SettingsFragment.KEY_PREF_NOTIFICATION_HOUR_PREFERENCE,"12")!!
            .toInt()
        //check value after change
        assertEquals(notificationPreference, true)
        assertEquals(notificationHour, 11)
    }
}