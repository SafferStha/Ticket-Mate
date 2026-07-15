package com.example.individual_project.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        /** How long before an event's start time the reminder fires. */
        private val REMINDER_LEAD_TIME_MILLIS = TimeUnit.HOURS.toMillis(2)
        private fun workName(bookingId: String) = "event_reminder_$bookingId"
    }

    /**
     * Schedules a local "starts soon" reminder for a confirmed booking. Uses a unique work
     * name per booking so re-booking or re-confirming replaces any prior reminder instead of
     * stacking duplicates, and does nothing if the event is already too close (or in the past)
     * for a lead-time reminder to make sense.
     */
    fun scheduleReminder(bookingId: String, eventId: String, eventTitle: String, venue: String, eventEpochMillis: Long?) {
        if (eventEpochMillis == null) return // date/time couldn't be parsed -- nothing to schedule against

        val fireAt = eventEpochMillis - REMINDER_LEAD_TIME_MILLIS
        val delay  = fireAt - System.currentTimeMillis()
        if (delay <= 0L) return // event starts too soon (or already started) for a lead-time reminder

        val data = Data.Builder()
            .putString(EventReminderWorker.KEY_EVENT_ID, eventId)
            .putString(EventReminderWorker.KEY_EVENT_TITLE, eventTitle)
            .putString(EventReminderWorker.KEY_VENUE, venue)
            .build()

        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(bookingId), ExistingWorkPolicy.REPLACE, request)
    }

    /** Cancels a previously scheduled reminder, e.g. when its booking is cancelled. */
    fun cancelReminder(bookingId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(bookingId))
    }
}
