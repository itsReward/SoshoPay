package com.soshopay.android.ui.component.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.iotapp.uiplayground.HomeMenu
import com.soshopay.android.ui.component.auth.LoginPage
import com.soshopay.android.ui.component.auth.SignUpPage

enum class LoginSignUpNavigationRoutes {
    Login,
    SignUp,
    Home,
}

fun NavGraphBuilder.loginDestination(
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onPopBackStack: () -> Boolean,
) {
    composable(LoginSignUpNavigationRoutes.Login.name) {
        LoginPage(onNavigateToHome, onNavigateToSignUp, onPopBackStack)
    }
}

fun NavGraphBuilder.signUpDestination(onPopBackStack: () -> Unit) {
    composable(LoginSignUpNavigationRoutes.SignUp.name) {
        SignUpPage(onPopBackStack)
    }
}
