package com.example.individual_project

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Swaps in [HiltTestApplication] so `@HiltAndroidTest` classes can replace production
 * bindings (repositories, Firebase instances) with test doubles via `@UninstallModules` /
 * `@BindValue`, instead of the real [TicketMateApplication] and its live Firebase wiring.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
