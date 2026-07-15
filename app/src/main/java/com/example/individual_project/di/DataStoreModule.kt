package com.example.individual_project.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Module-visible (not private) so EventReminderWorker -- which WorkManager instantiates
// directly, outside of Hilt's graph -- can read the same DataStore-backed preferences
// without a second, colliding "app_preferences" instance.
internal val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.appPreferencesDataStore
}
