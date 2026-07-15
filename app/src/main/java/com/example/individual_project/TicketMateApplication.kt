package com.example.individual_project

import android.app.Application
import com.example.individual_project.notifications.NotificationChannels
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TicketMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Disable Crashlytics collection in debug builds to avoid polluting production reports.
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        // Channels must exist before the first notification is posted on API 26+.
        NotificationChannels.createAll(this)
    }
}
