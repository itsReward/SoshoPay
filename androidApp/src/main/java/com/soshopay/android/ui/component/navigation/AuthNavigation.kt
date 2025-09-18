package com.soshopay.android.ui.component.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.iotapp.uiplayground.HomeMenu
import com.soshopay.android.ui.component.auth.EnhancedLoginScreen
import com.soshopay.android.ui.component.auth.EnhancedSignUpScreen
import com.soshopay.android.ui.component.auth.OtpVerificationScreen
import com.soshopay.android.ui.component.auth.PinSetupScreen

/**
 * Enhanced authentication navigation routes following the complete auth flow
 */
enum class AuthNavigationRoutes {
    Login,
    SignUp,
    OtpVerification,
    PinSetup,
    Home,
}

/**
 * Enhanced login destination with proper state management
 */
fun NavGraphBuilder.enhancedLoginDestination(
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    composable(AuthNavigationRoutes.Login.name) {
        EnhancedLoginScreen(
            onNavigateToHome = onNavigateToHome,
            onNavigateToSignUp = onNavigateToSignUp,
        )
    }
}

/**
 * Enhanced sign up destination with OTP flow
 */
fun NavGraphBuilder.enhancedSignUpDestination(
    onNavigateToOtpVerification: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onPopBackStack: () -> Unit,
) {
    composable(AuthNavigationRoutes.SignUp.name) {
        EnhancedSignUpScreen(
            onNavigateToOtpVerification = onNavigateToOtpVerification,
            onNavigateToLogin = onNavigateToLogin,
            onPopBackStack = onPopBackStack,
        )
    }
}

/**
 * OTP verification destination
 */
fun NavGraphBuilder.otpVerificationDestination(
    onNavigateToPinSetup: () -> Unit,
    onPopBackStack: () -> Unit,
) {
    composable(AuthNavigationRoutes.OtpVerification.name) {
        OtpVerificationScreen(
            onNavigateToPinSetup = onNavigateToPinSetup,
            onPopBackStack = onPopBackStack,
        )
    }
}

/**
 * PIN setup destination
 */
fun NavGraphBuilder.pinSetupDestination(
    onNavigateToHome: () -> Unit,
    onPopBackStack: () -> Unit,
) {
    composable(AuthNavigationRoutes.PinSetup.name) {
        PinSetupScreen(
            onNavigateToHome = onNavigateToHome,
            onPopBackStack = onPopBackStack,
        )
    }
}

/**
 * Home destination
 */
fun NavGraphBuilder.homeDestination() {
    composable(AuthNavigationRoutes.Home.name) {
        HomeMenu()
    }
}

// Navigation extension functions for NavController
fun NavController.navigateToLogin() {
    navigate(AuthNavigationRoutes.Login.name) {
        popUpTo(AuthNavigationRoutes.Login.name) {
            inclusive = true
        }
    }
}

fun NavController.navigateToSignUp() {
    navigate(AuthNavigationRoutes.SignUp.name)
}

fun NavController.navigateToOtpVerification() {
    navigate(AuthNavigationRoutes.OtpVerification.name)
}

fun NavController.navigateToPinSetup() {
    navigate(AuthNavigationRoutes.PinSetup.name) {
        popUpTo(AuthNavigationRoutes.SignUp.name) {
            inclusive = true
        }
    }
}

fun NavController.navigateToHome() {
    navigate(AuthNavigationRoutes.Home.name) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
