@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.domain.repository.SavedPaymentMethodRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class SavedPaymentMethodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SavedPaymentMethodRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val visa = SavedPaymentMethod(
        id = "pm1", userId = "user1", provider = "CARD",
        displayName = "My Visa", maskedIdentifier = "4242"
    )

    @Before
    fun setUp() {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
    }

    private fun createViewModel(): SavedPaymentMethodViewModel = SavedPaymentMethodViewModel(repository, firebaseAuth)

    @Test
    fun `loading saved methods populates the list`() = runTest {
        doAnswer { Resource.Success(listOf(visa)) }.`when`(repository).getSavedPaymentMethods(eq("user1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(visa), viewModel.uiState.value.methods)
    }

    @Test
    fun `empty saved methods is reflected as an empty, non-error list`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedPaymentMethod>()) }.`when`(repository).getSavedPaymentMethods(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(emptyList<SavedPaymentMethod>(), viewModel.uiState.value.methods)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onMaskedIdentifierChange strips non-digits and truncates to 4 characters`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedPaymentMethod>()) }.`when`(repository).getSavedPaymentMethods(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()

        viewModel.onMaskedIdentifierChange("42-42-99")

        assertEquals("4242", viewModel.formState.value?.maskedIdentifier)
    }

    @Test
    fun `submitForm rejects a blank display name without calling the repository`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedPaymentMethod>()) }.`when`(repository).getSavedPaymentMethods(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onMaskedIdentifierChange("4242")

        viewModel.submitForm()

        assertEquals("Give this method a name, e.g. \"My Visa\"", viewModel.formState.value?.error)
        verify(repository, times(0)).addSavedPaymentMethod(any())
    }

    @Test
    fun `submitForm rejects an identifier that is not exactly 4 digits`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedPaymentMethod>()) }.`when`(repository).getSavedPaymentMethods(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onDisplayNameChange("My Visa")

        viewModel.submitForm()

        assertEquals("Enter the last 4 digits", viewModel.formState.value?.error)
        verify(repository, times(0)).addSavedPaymentMethod(any())
    }

    @Test
    fun `submitForm success stores only the masked identifier, never a raw card number`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedPaymentMethod>()) }.`when`(repository).getSavedPaymentMethods(any())
        doAnswer { Resource.Success("pm1") }.`when`(repository).addSavedPaymentMethod(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onDisplayNameChange("My Visa")
        viewModel.onMaskedIdentifierChange("4242424242424242")

        viewModel.submitForm()
        advanceUntilIdle()

        val captor = org.mockito.kotlin.argumentCaptor<SavedPaymentMethod>()
        verify(repository).addSavedPaymentMethod(captor.capture())
        assertEquals("4242", captor.firstValue.maskedIdentifier)
        assertEquals(4, captor.firstValue.maskedIdentifier.length)
        assertNull(viewModel.formState.value)
    }

    @Test
    fun `deleteMethod removes the method and reloads on success`() = runTest {
        doAnswer { Resource.Success(listOf(visa)) }.`when`(repository).getSavedPaymentMethods(any())
        doAnswer { Resource.Success(Unit) }.`when`(repository).deleteSavedPaymentMethod(eq("user1"), eq("pm1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMethod("pm1")
        advanceUntilIdle()

        verify(repository).deleteSavedPaymentMethod(eq("user1"), eq("pm1"))
        verify(repository, times(2)).getSavedPaymentMethods(any())
    }

    @Test
    fun `deleteMethod failure surfaces the mapped error`() = runTest {
        doAnswer { Resource.Success(listOf(visa)) }.`when`(repository).getSavedPaymentMethods(any())
        doAnswer { Resource.Error("Delete failed") }.`when`(repository).deleteSavedPaymentMethod(any(), any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMethod("pm1")
        advanceUntilIdle()

        assertEquals("Delete failed", viewModel.uiState.value.error)
        assertFalse(viewModel.busyIds.value.contains("pm1"))
    }

    @Test
    fun `setDefaultMethod marks the method as default via the repository`() = runTest {
        doAnswer { Resource.Success(listOf(visa)) }.`when`(repository).getSavedPaymentMethods(any())
        doAnswer { Resource.Success(Unit) }.`when`(repository).setDefaultPaymentMethod(eq("user1"), eq("pm1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setDefaultMethod("pm1")
        advanceUntilIdle()

        verify(repository).setDefaultPaymentMethod(eq("user1"), eq("pm1"))
    }
}
