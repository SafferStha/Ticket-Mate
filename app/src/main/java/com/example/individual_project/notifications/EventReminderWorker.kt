package com.example.individual_project.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.individual_project.MainActivity
import com.example.individual_project.R
import com.example.individual_project.data.local.AppPreferenceKeys
import com.example.individual_project.di.appPreferencesDataStore
import kotlinx.coroutines.flow.first
import android.content.Context

/**
 * Posts a single local "your event starts soon" notification. Instantiated directly by
 * WorkManager (not through Hilt), so it reads preferences via the shared DataStore instance
 * rather than an injected repository -- see [appPreferencesDataStore]'s doc comment.
 */
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_EVENT_ID    = "eventId"
        const val KEY_EVENT_TITLE = "eventTitle"
        const val KEY_VENUE       = "venue"
    }

    override suspend fun doWork(): Result {
        val eventId    = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val eventTitle = inputData.getString(KEY_EVENT_TITLE) ?: "Your event"
        val venue      = inputData.getString(KEY_VENUE).orEmpty()

        val prefs = applicationContext.appPreferencesDataStore.data.first()
        val notificationsEnabled = prefs[AppPreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
        val remindersEnabled     = prefs[AppPreferenceKeys.EVENT_REMINDERS] ?: true
        if (!notificationsEnabled || !remindersEnabled) return Result.success()

        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No permission to post -- not an error, just nothing we can do.
            return Result.success()
        }

        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DEEP_LINK_EVENT_ID, eventId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            eventId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.EVENT_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(if (venue.isNotBlank()) "Starts soon at $venue" else "Starts soon")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(eventId.hashCode(), notification)

        return Result.success()
    }
}
