package com.example.individual_project.auth

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
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
import com.example.individual_project.ui.screens.ForgotPasswordScreen
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
 * Deterministic Compose UI tests for the login screen. The real screen composables are hosted
 * directly in a test Activity and backed by [FakeAuthRepository], avoiding MainActivity splash,
 * notification permission, and Firebase network state.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

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
                    composable(Screen.ForgotPassword.route) {
                        ForgotPasswordScreen(navController = navController, viewModel = authViewModel)
                    }
                    composable(Screen.VerifyEmail.route) { Text("Verify Your Email") }
                    composable(Screen.Dashboard.route) { Text("Dashboard") }
                }
            }
        }
    }

    @Test
    fun loginScreen_rendersEmailPasswordAndSubmit() {
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).assertIsDisplayed()
    }

    @Test
    fun emailField_acceptsTypedInput() {
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("user@example.com")
        composeRule.onNodeWithText("user@example.com").assertIsDisplayed()
    }

    @Test
    fun passwordField_acceptsTypedInput() {
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput("secret123")
        // Password is masked, but the field must accept input without crashing.
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).assertIsDisplayed()
    }

    @Test
    fun submit_withEmptyEmail_showsValidationError() {
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()
        composeRule.onNodeWithText("Email is required.").assertIsDisplayed()
    }

    @Test
    fun submit_withEmptyPassword_showsValidationError() {
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("user@example.com")
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()
        composeRule.onNodeWithText("Password is required.").assertIsDisplayed()
    }

    @Test
    fun submit_withInvalidEmailFormat_showsValidationError() {
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("not-an-email")
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput("secret123")
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()
        composeRule.onNodeWithText("Enter a valid email address.").assertIsDisplayed()
    }

    @Test
    fun submit_repositoryFailure_showsMappedError() {
        fakeAuthRepository.nextLoginResult = Resource.Error("Incorrect email or password. Please try again.")
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("user@example.com")
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput("wrong-password")
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()

        composeRule.onNodeWithTag(TestTags.LOGIN_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText("Incorrect email or password. Please try again.").assertIsDisplayed()
    }

    @Test
    fun registerButton_navigatesToRegistrationScreen() {
        composeRule.onNodeWithTag(TestTags.LOGIN_REGISTER).performClick()
        composeRule.onNodeWithText("Join TicketMate").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordLink_navigatesToForgotPasswordScreen() {
        composeRule.onNodeWithTag(TestTags.LOGIN_FORGOT_PASSWORD).performClick()
        composeRule.onNodeWithText("Reset Password").assertIsDisplayed()
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
