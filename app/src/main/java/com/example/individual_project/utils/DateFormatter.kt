package com.example.individual_project.utils

import java.text.SimpleDateFormat
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
}
