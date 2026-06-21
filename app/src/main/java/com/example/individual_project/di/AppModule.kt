package com.example.individual_project.di

import com.example.individual_project.data.repository.AuthRepositoryImpl
import com.example.individual_project.data.repository.BookingRepositoryImpl
import com.example.individual_project.data.repository.EventRepositoryImpl
import com.example.individual_project.data.repository.PaymentRepositoryImpl
import com.example.individual_project.data.repository.TicketRepositoryImpl
import com.example.individual_project.data.repository.UserRepositoryImpl
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ─── Firebase primitive providers ─────────────────────────────────────────────
// These are the only place in the entire app where Firebase.getInstance() is called.
// Everything else receives these via injection.

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

// ─── Repository bindings ───────────────────────────────────────────────────────
// @Binds tells Hilt "when something asks for the interface, give the implementation".
// Hilt constructs the Impl classes automatically via their @Inject constructors.

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindTicketRepository(impl: TicketRepositoryImpl): TicketRepository
}
