package com.example.individual_project.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class DateFormatterTest {

    @Test
    fun `parseEventDateTimeMillis returns null for a blank date`() {
        assertNull(DateFormatter.parseEventDateTimeMillis("", "18:30"))
    }

    @Test
    fun `parseEventDateTimeMillis returns null for an unrecognized date format`() {
        assertNull(DateFormatter.parseEventDateTimeMillis("not a date", "18:30"))
    }

    @Test
    fun `parseEventDateTimeMillis parses an ISO date with a 24-hour time`() {
        val millis = DateFormatter.parseEventDateTimeMillis("2026-08-20", "18:30")
        assertNotNull(millis)

        val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = millis!! }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH))
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `parseEventDateTimeMillis falls back to midnight when time is blank`() {
        val millis = DateFormatter.parseEventDateTimeMillis("2026-08-20", "")
        assertNotNull(millis)
        val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = millis!! }
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `parseEventDateTimeMillis still returns the date when only the time is unrecognized`() {
        val millis = DateFormatter.parseEventDateTimeMillis("2026-08-20", "not a time")
        assertNotNull(millis)
    }

    @Test
    fun `formatMemberSince returns an em dash for a non-positive timestamp`() {
        assertEquals("—", DateFormatter.formatMemberSince(0L))
        assertEquals("—", DateFormatter.formatMemberSince(-1L))
    }

    @Test
    fun `formatMemberSince formats a real timestamp as Month Year`() {
        val cal = Calendar.getInstance(Locale.US).apply {
            set(2025, Calendar.MARCH, 15, 0, 0, 0)
        }
        val formatted = DateFormatter.formatMemberSince(cal.timeInMillis)
        assertTrue(formatted.contains("2025"))
        assertTrue(formatted.contains("March"))
    }
}
