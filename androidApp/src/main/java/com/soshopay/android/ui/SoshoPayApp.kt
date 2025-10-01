package com.soshopay.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.soshopay.android.ui.component.navigation.AuthNavigationRoutes
import com.soshopay.android.ui.component.navigation.LoginSignUpNavigationRoutes
import com.soshopay.android.ui.component.navigation.enhancedLoginDestination
import com.soshopay.android.ui.component.navigation.enhancedSignUpDestination
import com.soshopay.android.ui.component.navigation.homeDestination
import com.soshopay.android.ui.component.navigation.loginDestination
import com.soshopay.android.ui.component.navigation.navigateToHome
import com.soshopay.android.ui.component.navigation.navigateToLogin
import com.soshopay.android.ui.component.navigation.navigateToOtpVerification
import com.soshopay.android.ui.component.navigation.navigateToPinSetup
import com.soshopay.android.ui.component.navigation.navigateToSignUp
import com.soshopay.android.ui.component.navigation.onBoardDestination
import com.soshopay.android.ui.component.navigation.otpVerificationDestination
import com.soshopay.android.ui.component.navigation.pinSetupDestination
import com.soshopay.android.ui.component.navigation.signUpDestination
import com.soshopay.android.ui.component.onBoard.CashLoansOnBoard
import com.soshopay.android.ui.component.onBoard.DeviceLoansOnBoard
import com.soshopay.android.ui.screen.LoginScreen
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun SoshoPayApp() {
    val navController = rememberNavController()

    // Start with Login screen - the ViewModel will handle checking if user is already logged in
    NavHost(
        navController = navController,
        startDestination = AuthNavigationRoutes.Login.name,
    ) {
        // Onboarding destination (if needed)
        onBoardDestination(
            onNavigateToLogin = {
                navController.navigateToLogin()
            },
        )

        // Enhanced Login Screen
        enhancedLoginDestination(
            onNavigateToHome = {
                navController.navigateToHome()
            },
            onNavigateToSignUp = {
                navController.navigateToSignUp()
            },
        )

        // Enhanced Sign Up Screen
        enhancedSignUpDestination(
            onNavigateToOtpVerification = {
                navController.navigateToOtpVerification()
            },
            onNavigateToLogin = {
                navController.navigateToLogin()
            },
            onPopBackStack = {
                navController.popBackStack()
            },
        )

        // OTP Verification Screen
        otpVerificationDestination(
            onNavigateToPinSetup = {
                navController.navigateToPinSetup()
            },
            onPopBackStack = {
                navController.popBackStack()
            },
        )

        // PIN Setup Screen
        pinSetupDestination(
            onNavigateToHome = {
                navController.navigateToHome()
            },
            onPopBackStack = {
                navController.popBackStack()
            },
        )

        // Home Screen
        homeDestination()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppStart(onNavigateToLogin: () -> Unit) {
    SoshoPayTheme {
        val pagerState = rememberPagerState(pageCount = { 2 })

        HorizontalPager(state = pagerState) { currentPage ->
            when (currentPage) {
                0 ->
                    DeviceLoansOnBoard(
                        pagerState.currentPage,
                        pagerState,
                        onNavigateToLogin,
                    )
                1 ->
                    CashLoansOnBoard(
                        pagerState.currentPage,
                        pagerState,
                        onNavigateToLogin,
                    )
            }
        }
    }
}
