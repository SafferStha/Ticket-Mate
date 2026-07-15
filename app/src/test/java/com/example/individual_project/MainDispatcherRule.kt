package com.example.individual_project

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Points viewModelScope's Dispatchers.Main at a controllable TestDispatcher for the duration
 * of a test. Uses UnconfinedTestDispatcher, not StandardTestDispatcher, deliberately:
 * viewModelScope actually runs on Dispatchers.Main.immediate in production, which executes a
 * coroutine synchronously up to its first suspension point rather than always queuing it.
 * StandardTestDispatcher always queues, which would make two back-to-back calls to a guarded
 * ViewModel function both pass the guard before either had a chance to flip its "in flight"
 * flag -- a false test failure, not a real bug. UnconfinedTestDispatcher's eager-start behavior
 * matches Main.immediate closely enough to test that guard correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}
