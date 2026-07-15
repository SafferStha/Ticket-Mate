package com.example.individual_project.testdi

import com.example.individual_project.di.RepositoryModule
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.domain.repository.SavedLocationRepository
import com.example.individual_project.domain.repository.SavedPaymentMethodRepository
import com.example.individual_project.domain.repository.SearchRepository
import com.example.individual_project.domain.repository.SettingsRepository
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces [RepositoryModule] for every `@HiltAndroidTest`: every repository interface resolves
 * to an in-memory fake instead of the real Firebase-backed implementation, so ordinary Compose
 * UI tests never touch the network. Each fake is also exposed by its concrete type so a test can
 * `@Inject` it directly to arrange state or assert interactions.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object TestAppModule {

    @Provides
    @Singleton
    fun provideFakeAuthRepository(): FakeAuthRepository = FakeAuthRepository()

    @Provides
    fun provideAuthRepository(fake: FakeAuthRepository): AuthRepository = fake

    @Provides
    @Singleton
    fun provideFakeEventRepository(): FakeEventRepository = FakeEventRepository()

    @Provides
    fun provideEventRepository(fake: FakeEventRepository): EventRepository = fake

    @Provides
    @Singleton
    fun provideFakeBookingRepository(): FakeBookingRepository = FakeBookingRepository()

    @Provides
    fun provideBookingRepository(fake: FakeBookingRepository): BookingRepository = fake

    @Provides
    @Singleton
    fun provideFakePaymentRepository(): FakePaymentRepository = FakePaymentRepository()

    @Provides
    fun providePaymentRepository(fake: FakePaymentRepository): PaymentRepository = fake

    @Provides
    @Singleton
    fun provideFakeUserRepository(): FakeUserRepository = FakeUserRepository()

    @Provides
    fun provideUserRepository(fake: FakeUserRepository): UserRepository = fake

    @Provides
    @Singleton
    fun provideFakeTicketRepository(): FakeTicketRepository = FakeTicketRepository()

    @Provides
    fun provideTicketRepository(fake: FakeTicketRepository): TicketRepository = fake

    @Provides
    @Singleton
    fun provideFakeSearchRepository(): FakeSearchRepository = FakeSearchRepository()

    @Provides
    fun provideSearchRepository(fake: FakeSearchRepository): SearchRepository = fake

    @Provides
    @Singleton
    fun provideFakeSettingsRepository(): FakeSettingsRepository = FakeSettingsRepository()

    @Provides
    fun provideSettingsRepository(fake: FakeSettingsRepository): SettingsRepository = fake

    @Provides
    @Singleton
    fun provideFakeSavedLocationRepository(): FakeSavedLocationRepository = FakeSavedLocationRepository()

    @Provides
    fun provideSavedLocationRepository(fake: FakeSavedLocationRepository): SavedLocationRepository = fake

    @Provides
    @Singleton
    fun provideFakeSavedPaymentMethodRepository(): FakeSavedPaymentMethodRepository = FakeSavedPaymentMethodRepository()

    @Provides
    fun provideSavedPaymentMethodRepository(fake: FakeSavedPaymentMethodRepository): SavedPaymentMethodRepository = fake
}
