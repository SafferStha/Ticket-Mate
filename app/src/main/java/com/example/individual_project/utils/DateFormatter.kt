package com.example.individual_project.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val inputDateFormats = listOf(
        "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy",
        "MM-dd-yyyy", "yyyy/MM/dd", "MMM dd, yyyy",
        "MMMM dd, yyyy", "dd MMM yyyy", "d MMM yyyy"
    )

    private val inputTimeFormats = listOf(
        "HH:mm", "HH:mm:ss", "h:mm a", "hh:mm a",
        "h:mma", "hh:mma", "H:mm", "h:mm"
    )

    fun formatDate(raw: String): String {
        if (raw.isBlank()) return raw
        val displayFmt = SimpleDateFormat("EEE, MMM d", Locale.US)
        for (fmt in inputDateFormats) {
            runCatching {
                val parsed = SimpleDateFormat(fmt, Locale.US).parse(raw.trim())
                if (parsed != null) return displayFmt.format(parsed)
            }
        }
        return raw
    }

    /** "Member since March 2025" style label from an epoch-millis timestamp. */
    fun formatMemberSince(epochMillis: Long): String {
        if (epochMillis <= 0L) return "—"
        return SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(epochMillis))
    }

    fun formatTime(raw: String): String {
        if (raw.isBlank()) return raw
        val displayFmt = SimpleDateFormat("h:mm a", Locale.US)
        for (fmt in inputTimeFormats) {
            runCatching {
                val parsed = SimpleDateFormat(fmt, Locale.US).parse(raw.trim())
                if (parsed != null) return displayFmt.format(parsed)
            }
        }
        return raw
    }

    /**
     * Best-effort combined date+time parse to epoch millis, for anything that needs an
     * absolute instant (e.g. scheduling a reminder). Returns null rather than guessing when
     * neither the date nor the time half is recognized -- callers must treat that as
     * "can't schedule against this event" rather than defaulting to some arbitrary instant.
     */
    fun parseEventDateTimeMillis(date: String, time: String): Long? {
        if (date.isBlank()) return null
        val parsedDate = inputDateFormats.firstNotNullOfOrNull { fmt ->
            runCatching { SimpleDateFormat(fmt, Locale.US).parse(date.trim()) }.getOrNull()
        } ?: return null

        if (time.isBlank()) return parsedDate.time

        val parsedTime = inputTimeFormats.firstNotNullOfOrNull { fmt ->
            runCatching { SimpleDateFormat(fmt, Locale.US).parse(time.trim()) }.getOrNull()
        } ?: return parsedDate.time

        val dateCal = java.util.Calendar.getInstance().also { it.time = parsedDate }
        val timeCal = java.util.Calendar.getInstance().also { it.time = parsedTime }
        dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
        dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
        dateCal.set(java.util.Calendar.SECOND, 0)
        dateCal.set(java.util.Calendar.MILLISECOND, 0)
        return dateCal.timeInMillis
    }
}
