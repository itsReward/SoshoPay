package com.soshopay.android.ui.component.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.soshopay.android.ui.AppStart

enum class OnBoardNavigationRoutes {
    OnBoard,
}

fun NavGraphBuilder.onBoardDestination(onNavigateToLogin: () -> Unit) {
    composable(OnBoardNavigationRoutes.OnBoard.name){
        AppStart(onNavigateToLogin)
    }
}

fun NavController.navigateToLogin(){
    navigate(LoginSignUpNavigationRoutes.Login.name)
}


