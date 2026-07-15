@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.domain.repository.SavedLocationRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class SavedLocationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SavedLocationRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val home = SavedLocation(id = "loc1", userId = "user1", label = "Home", address = "123 St", city = "Kathmandu")

    @Before
    fun setUp() {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
    }

    private fun createViewModel(): SavedLocationViewModel = SavedLocationViewModel(repository, firebaseAuth)

    @Test
    fun `loading saved locations populates the list`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(eq("user1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(home), viewModel.uiState.value.locations)
    }

    @Test
    fun `empty saved locations is reflected as an empty, non-error list`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedLocation>()) }.`when`(repository).getSavedLocations(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(emptyList<SavedLocation>(), viewModel.uiState.value.locations)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `not logged in surfaces an explanatory error without calling the repository`() = runTest {
        doAnswer { null }.`when`(firebaseAuth).currentUser
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Not logged in", viewModel.uiState.value.error)
        verify(repository, times(0)).getSavedLocations(any())
    }

    @Test
    fun `submitForm rejects missing required fields without calling the repository`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedLocation>()) }.`when`(repository).getSavedLocations(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onLabelChange("Home")
        // address and city left blank

        viewModel.submitForm()

        assertEquals("Label, address, and city are all required", viewModel.formState.value?.error)
        verify(repository, times(0)).addSavedLocation(any())
    }

    @Test
    fun `submitForm rejects a duplicate label`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onLabelChange("Home")
        viewModel.onAddressChange("456 Ave")
        viewModel.onCityChange("Pokhara")

        viewModel.submitForm()

        assertEquals("You already have a location saved with this name", viewModel.formState.value?.error)
        verify(repository, times(0)).addSavedLocation(any())
    }

    @Test
    fun `submitForm add success dismisses the form and reloads locations`() = runTest {
        doAnswer { Resource.Success(emptyList<SavedLocation>()) }.`when`(repository).getSavedLocations(any())
        doAnswer { Resource.Success("loc1") }.`when`(repository).addSavedLocation(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startAdd()
        viewModel.onLabelChange("Home")
        viewModel.onAddressChange("123 St")
        viewModel.onCityChange("Kathmandu")

        viewModel.submitForm()
        advanceUntilIdle()

        assertNull(viewModel.formState.value)
        verify(repository).addSavedLocation(any())
        verify(repository, times(2)).getSavedLocations(any())
    }

    @Test
    fun `submitForm edit calls updateSavedLocation instead of addSavedLocation`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        doAnswer { Resource.Success(Unit) }.`when`(repository).updateSavedLocation(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startEdit(home)
        viewModel.onAddressChange("New Address")

        viewModel.submitForm()
        advanceUntilIdle()

        verify(repository).updateSavedLocation(any())
        verify(repository, times(0)).addSavedLocation(any())
    }

    @Test
    fun `deleteLocation removes the location and reloads on success`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        doAnswer { Resource.Success(Unit) }.`when`(repository).deleteSavedLocation(eq("user1"), eq("loc1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteLocation("loc1")
        advanceUntilIdle()

        verify(repository).deleteSavedLocation(eq("user1"), eq("loc1"))
        verify(repository, times(2)).getSavedLocations(any())
    }

    @Test
    fun `deleteLocation failure surfaces the mapped error`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        doAnswer { Resource.Error("Delete failed") }.`when`(repository).deleteSavedLocation(any(), any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteLocation("loc1")
        advanceUntilIdle()

        assertEquals("Delete failed", viewModel.uiState.value.error)
    }

    @Test
    fun `setDefaultLocation marks the location as default via the repository`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        doAnswer { Resource.Success(Unit) }.`when`(repository).setDefaultLocation(eq("user1"), eq("loc1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setDefaultLocation("loc1")
        advanceUntilIdle()

        verify(repository).setDefaultLocation(eq("user1"), eq("loc1"))
    }

    @Test
    fun `deleteLocation guards against a duplicate call while the same id is busy`() = runTest {
        doAnswer { Resource.Success(listOf(home)) }.`when`(repository).getSavedLocations(any())
        val suspendingRepository = object : SavedLocationRepository by repository {
            override suspend fun deleteSavedLocation(userId: String, locationId: String): Resource<Unit> {
                kotlinx.coroutines.delay(1)
                return repository.deleteSavedLocation(userId, locationId)
            }
        }
        doAnswer { Resource.Success(Unit) }.`when`(repository).deleteSavedLocation(any(), any())
        val viewModel = SavedLocationViewModel(suspendingRepository, firebaseAuth)
        advanceUntilIdle()

        viewModel.deleteLocation("loc1")
        assertTrue("loc1" in viewModel.busyIds.value)
        viewModel.deleteLocation("loc1")
        advanceUntilIdle()

        verify(repository, times(1)).deleteSavedLocation(any(), any())
    }
}
