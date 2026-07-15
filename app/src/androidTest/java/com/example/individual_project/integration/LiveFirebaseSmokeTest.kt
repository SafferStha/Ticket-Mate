package com.example.individual_project.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.individual_project.MainActivity
import com.example.individual_project.ui.testtags.TestTags
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Optional, opt-in end-to-end smoke test against a *real* Firebase project (or emulator). This
 * is deliberately kept out of the default instrumented run: it needs network access, a
 * previously-provisioned test account, and can be flaky under rate limiting or backend data
 * drift. Ordinary UI behavior (validation, navigation, error mapping) is covered instead by
 * the deterministic tests in `auth/`, which fake every repository and never touch the network.
 *
 * Enable explicitly:
 * ```
 * ./gradlew connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.runLiveFirebaseTests=true \
 *   -Pandroid.testInstrumentationRunnerArguments.tm_login_email="you@example.com" \
 *   -Pandroid.testInstrumentationRunnerArguments.tm_login_password="..." \
 *   -Pandroid.testInstrumentationRunnerArguments.tm_event_title="An event title known to exist"
 * ```
 * Credentials are read only from instrumentation arguments (CI secrets / local-only shell
 * history) -- never hardcode them here.
 */
@RunWith(AndroidJUnit4::class)
class LiveFirebaseSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val args = InstrumentationRegistry.getArguments()

    @Before
    fun setUp() {
        assumeTrue(
            "Live Firebase tests are opt-in; pass -Pandroid.testInstrumentationRunnerArguments.runLiveFirebaseTests=true to enable.",
            args.getString("runLiveFirebaseTests").toBoolean()
        )
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun login_withConfiguredCredentials_opensDashboard() {
        val email = requiredArgument("tm_login_email")
        val password = requiredArgument("tm_login_password")

        waitForAnyText("Welcome Back!", "Sign in to continue")
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput(email)
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput(password)
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()

        waitForAnyText("Discover Events 🎫", "TicketMate")
        composeRule.onNodeWithText("TicketMate").assertIsDisplayed()
    }

    @Test
    fun login_withInvalidCredentials_showsMappedError() {
        waitForAnyText("Welcome Back!", "Sign in to continue")
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("invalid-ticketmate-user@example.com")
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput("WrongPassword123!")
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()

        waitForAnyText(
            "Incorrect email or password. Please try again.",
            "No account found with this email.",
            "Network error. Please check your connection and try again.",
            "Too many attempts. Please wait a moment and try again."
        )
    }

    @Test
    fun eventDetails_forConfiguredEvent_display() {
        val email = requiredArgument("tm_login_email")
        val password = requiredArgument("tm_login_password")
        val eventTitle = requiredArgument("tm_event_title")

        waitForAnyText("Welcome Back!", "Sign in to continue")
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput(email)
        composeRule.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput(password)
        composeRule.onNodeWithTag(TestTags.LOGIN_SUBMIT).performClick()
        waitForAnyText("Discover Events 🎫", "TicketMate")

        composeRule.onNodeWithText(eventTitle, substring = true).performClick()

        composeRule.onNodeWithText("Event Details").assertIsDisplayed()
        composeRule.onNodeWithText(eventTitle, substring = true).assertIsDisplayed()
    }

    @Test
    fun registration_withFreshEmail_opensVerifyEmail() {
        val email = requiredArgument("tm_register_email")
        val password = args.getString("tm_register_password").takeUnless { it.isNullOrBlank() } ?: "TicketMate123!"

        waitForAnyText("Welcome Back!", "Sign in to continue")
        composeRule.onNodeWithTag(TestTags.LOGIN_REGISTER).performClick()
        composeRule.onNodeWithText("Create Account").assertIsDisplayed()

        composeRule.onNodeWithTag(TestTags.REGISTER_NAME).performTextInput("TicketMate Instrumented User")
        composeRule.onNodeWithTag(TestTags.REGISTER_EMAIL).performTextInput(email)
        composeRule.onNodeWithTag(TestTags.REGISTER_PASSWORD).performTextInput(password)
        composeRule.onNodeWithTag(TestTags.REGISTER_CONFIRM_PASSWORD).performTextInput(password)
        composeRule.onAllNodes(isToggleable()).onFirst().performClick()
        composeRule.onNodeWithTag(TestTags.REGISTER_SUBMIT).performClick()

        waitForAnyText("Verify Your Email")
        composeRule.onNodeWithText("Verify Your Email").assertIsDisplayed()
    }

    private fun waitForAnyText(vararg texts: String) {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            texts.any { text ->
                composeRule.onAllNodesWithText(text, substring = false).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }

    private fun requiredArgument(name: String): String {
        val value = args.getString(name).orEmpty()
        assumeTrue("Instrumentation argument '$name' is required for this live Firebase test.", value.isNotBlank())
        return value
    }
}
