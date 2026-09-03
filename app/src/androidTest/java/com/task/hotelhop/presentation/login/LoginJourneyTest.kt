package com.task.hotelhop.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.task.hotelhop.R
import com.task.hotelhop.domain.usecase.user.EnterGuestModeUseCase
import com.task.hotelhop.domain.usecase.user.LoginWithEmailUseCase
import com.task.hotelhop.domain.usecase.user.SignInWithGoogleUseCase
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.testutil.FakeUserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginJourneyTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signIn_withoutCredentials_showsRequiredFieldErrors() {
        setLoginScreen()

        composeRule.onNodeWithText(string(R.string.login_action)).performClick()

        composeRule.onAllNodesWithText(string(R.string.error_validation_required))
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(2, nodes.size) }
        composeRule.onNodeWithText(string(R.string.login_title)).assertIsDisplayed()
    }

    @Test
    fun continueAsGuest_entersGuestModeAndNavigatesHome() {
        val repository = FakeUserRepository()
        var navigatedHome = false
        setLoginScreen(repository) { navigatedHome = true }

        composeRule.onNodeWithText(string(R.string.login_continue_guest)).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { navigatedHome }

        assertTrue(navigatedHome)
        assertEquals(1, repository.enterGuestCalls)
        assertEquals(null, repository.lastLoginEmail)
    }

    @Test
    fun validEmailAndPassword_signsInAndNavigatesHome() {
        val repository = FakeUserRepository()
        var navigatedHome = false
        setLoginScreen(repository) { navigatedHome = true }

        composeRule.onNodeWithText(string(R.string.login_email)).performTextInput("guest@hotelhop.test")
        composeRule.onNodeWithText(string(R.string.login_password)).performTextInput("secret1")
        composeRule.onNodeWithText(string(R.string.login_action)).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { navigatedHome }

        assertTrue(navigatedHome)
        assertEquals("guest@hotelhop.test", repository.lastLoginEmail)
        assertEquals(0, repository.enterGuestCalls)
    }

    private fun setLoginScreen(
        repository: FakeUserRepository = FakeUserRepository(),
        onHome: () -> Unit = {}
    ) {
        val viewModel = LoginViewModel(
            loginWithEmailUseCase = LoginWithEmailUseCase(repository),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(repository),
            enterGuestModeUseCase = EnterGuestModeUseCase(repository)
        )
        composeRule.setContent {
            HotelHopTheme {
                LoginScreen(
                    onNavigateToHome = onHome,
                    onNavigateToRegister = {},
                    viewModel = viewModel
                )
            }
        }
    }

    private fun string(resId: Int): String {
        return androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(resId)
    }
}
