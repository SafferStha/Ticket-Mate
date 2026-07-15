package com.example.individual_project.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat

object NotificationChannels {

    const val BOOKING_UPDATES = "channel_booking_updates"
    const val PAYMENT_UPDATES = "channel_payment_updates"
    const val EVENT_REMINDERS = "channel_event_reminders"
    const val PROMOTIONAL    = "channel_promotional"

    /** Registers all channels. Safe to call repeatedly -- creating an existing channel id
     *  with the same settings is a no-op per the platform API. Must run before the first
     *  notification is posted on API 26+, so it's called from Application.onCreate(). */
    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return

        val channels = listOf(
            NotificationChannel(
                BOOKING_UPDATES,
                "Booking updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Booking confirmations, changes, and cancellations" },

            NotificationChannel(
                PAYMENT_UPDATES,
                "Payment updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Payment success, failure, and refund notices" },

            NotificationChannel(
                EVENT_REMINDERS,
                "Event reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Reminders before events you've booked" },

            NotificationChannel(
                PROMOTIONAL,
                "Promotions",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Offers and announcements" }
        )

        manager.createNotificationChannels(channels)
    }
}
