package com.example.individual_project.auth

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.individual_project.testdi.FakeAuthRepository
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.screens.LoginScreen
import com.example.individual_project.ui.screens.RegisterScreen
import com.example.individual_project.ui.testtags.TestTags
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.viewmodel.AuthViewModel
import com.example.individual_project.utils.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Deterministic Compose UI tests for the registration screen. The real auth screens are hosted
 * directly in a test Activity and backed by [FakeAuthRepository], so no Firebase account or
 * MainActivity splash flow is involved.
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository().also { it.reset() }
        val authViewModel = createAuthViewModel(fakeAuthRepository)

        composeRule.setContent {
            IndividualProjectTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Login.route) {
                    composable(Screen.Login.route) {
                        LoginScreen(navController = navController, viewModel = authViewModel)
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(navController = navController, viewModel = authViewModel)
                    }
                    composable(Screen.VerifyEmail.route) { Text("Verify Your Email") }
                    composable(Screen.Dashboard.route) { Text("Dashboard") }
                }
            }
        }

        composeRule.onNodeWithTag(TestTags.LOGIN_REGISTER).performClick()
        composeRule.onNodeWithText("Join TicketMate").assertIsDisplayed()
    }

    @Test
    fun registerScreen_renders() {
        composeRule.onNodeWithText("Join TicketMate").assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.REGISTER_NAME).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).assertIsDisplayed()
    }

    @Test
    fun backButton_returnsToLogin() {
        composeRule.onNodeWithTag(TestTags.REGISTER_BACK).performClick()
        composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }

    @Test
    fun submit_withEmptyFields_showsValidationErrors() {
        composeRule.onNodeWithTag(TestTags.REGISTER_TERMS).performClick()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        composeRule.onNodeWithText("Full name is required").assertIsDisplayed()
        composeRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun submit_withInvalidEmail_showsValidationError() {
        fillValidFormExcept(email = "not-an-email")
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        composeRule.onNodeWithText("Enter a valid email address").assertIsDisplayed()
    }

    @Test
    fun submit_withMismatchedPasswords_showsValidationError() {
        composeRule.onNodeWithTag(TestTags.REGISTER_NAME).performTextInput("Jane Doe")
        composeRule.onNodeWithTag(TestTags.REGISTER_EMAIL).performTextInput("jane@example.com")
        composeRule.onNodeWithTag(TestTags.REGISTER_PASSWORD).performTextInput("Password1!")
        composeRule.onNodeWithTag(TestTags.REGISTER_CONFIRM_PASSWORD).performTextInput("Different1!")
        composeRule.onNodeWithTag(TestTags.REGISTER_TERMS).performClick()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        composeRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun submit_withoutAcceptingTerms_isDisabled() {
        // The submit button is disabled (enabled = acceptTerms), so this asserts the disabled
        // state directly rather than performing a click a disabled node may not expose.
        composeRule.onNodeWithTag(TestTags.REGISTER_NAME).performTextInput("Jane Doe")
        composeRule.onNodeWithTag(TestTags.REGISTER_EMAIL).performTextInput("jane@example.com")
        composeRule.onNodeWithTag(TestTags.REGISTER_PASSWORD).performTextInput("Password1!")
        composeRule.onNodeWithTag(TestTags.REGISTER_CONFIRM_PASSWORD).performTextInput("Password1!")

        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).assertIsNotEnabled()
        assert(fakeAuthRepository.lastRegisterEmail == null)
    }

    @Test
    fun submit_success_navigatesToVerifyEmail() {
        fillValidFormExcept()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        composeRule.onNodeWithText("Verify Your Email").assertIsDisplayed()
    }

    @Test
    fun submit_existingAccountFailure_showsMappedMessage() {
        fakeAuthRepository.nextRegisterResult =
            Resource.Error("An account with this email already exists. Try signing in instead.")
        fillValidFormExcept()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        composeRule.onNodeWithTag(TestTags.REGISTER_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText("An account with this email already exists. Try signing in instead.")
            .assertIsDisplayed()
    }

    private fun fillValidFormExcept(email: String = "jane@example.com") {
        composeRule.onNodeWithTag(TestTags.REGISTER_NAME).performTextInput("Jane Doe")
        composeRule.onNodeWithTag(TestTags.REGISTER_EMAIL).performTextInput(email)
        composeRule.onNodeWithTag(TestTags.REGISTER_PASSWORD).performTextInput("Password1!")
        composeRule.onNodeWithTag(TestTags.REGISTER_CONFIRM_PASSWORD).performTextInput("Password1!")
        composeRule.onNodeWithTag(TestTags.REGISTER_TERMS).performClick()
    }

    private fun createAuthViewModel(repository: FakeAuthRepository): AuthViewModel {
        val authStateManager = mockk<AuthStateManager>(relaxed = true) {
            every { authState } returns MutableStateFlow(AuthState.Unauthenticated)
        }
        val adminStateManager = mockk<AdminStateManager>(relaxed = true) {
            every { isAdmin } returns MutableStateFlow(false)
        }
        return AuthViewModel(
            authRepository = repository,
            authStateManager = authStateManager,
            adminStateManager = adminStateManager
        )
    }
}
