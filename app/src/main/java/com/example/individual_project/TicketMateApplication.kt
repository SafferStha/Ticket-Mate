package com.example.individual_project

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TicketMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Disable Crashlytics collection in debug builds to avoid polluting production reports.
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
