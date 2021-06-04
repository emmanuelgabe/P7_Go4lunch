package com.emmanuel.go4lunch

import com.emmanuel.go4lunch.utils.MAX_WITH_ICON
import com.emmanuel.go4lunch.utils.getPhotoUrlFromReference
import com.emmanuel.go4lunch.utils.isSameDay
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.SimpleDateFormat
import java.util.*

@RunWith(JUnit4::class)
class UtilsUnitTest {

    @Test
    fun generatePhotoUrlFromReference_returnCorrectUrl() {
        val photoReference =
            "ATtYBwIEVL0W-IqUXJnGgu306P-pSPw6PQBC5ZFSH1zpfh9sV9E56AB6C8YNkMKBpHE_dkoXISemBbYnbWPzE0mNhgfE1c2Ra0qqS2DonEiyhHH3-PaUCCPyzs3nc_LB2Q328gJEWfd8qQEQ3uY6x85pLp8T0qt12wlwrW6rryNsnGleBoa3"
        val expectedUrl =
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=60&photoreference=ATtYBwIEVL0W-IqUXJnGgu306P-pSPw6PQBC5ZFSH1zpfh9sV9E56AB6C8YNkMKBpHE_dkoXISemBbYnbWPzE0mNhgfE1c2Ra0qqS2DonEiyhHH3-PaUCCPyzs3nc_LB2Q328gJEWfd8qQEQ3uY6x85pLp8T0qt12wlwrW6rryNsnGleBoa3&key=A"
        assertEquals(expectedUrl,  getPhotoUrlFromReference(photoReference, MAX_WITH_ICON).substring(0,262))
    }

    @Test
    fun dateIsSameDay_correspondToTheDateInput() {
        val df = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        val date1: Date = df.parse("2020-01-10 10:20:55")!!
        val date2: Date = df.parse("2020-01-10 09:15:30")!!
        val date3: Date = df.parse("2020-01-11 02:01:01")!!
        assertTrue(isSameDay(date1, date2))
        assertFalse(isSameDay(date3, date1))
    }
}