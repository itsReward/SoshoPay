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
import com.soshopay.android.ui.component.navigation.LoginSignUpNavigationRoutes
import com.soshopay.android.ui.component.navigation.homeDestination
import com.soshopay.android.ui.component.navigation.loginDestination
import com.soshopay.android.ui.component.navigation.navigateToHome
import com.soshopay.android.ui.component.navigation.navigateToLogin
import com.soshopay.android.ui.component.navigation.navigateToSignUp
import com.soshopay.android.ui.component.navigation.onBoardDestination
import com.soshopay.android.ui.component.navigation.signUpDestination
import com.soshopay.android.ui.component.onBoard.CashLoansOnBoard
import com.soshopay.android.ui.component.onBoard.DeviceLoansOnBoard
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.screen.LoginScreen

@Composable
fun SoshoPayApp(){
    val navController = rememberNavController()

    //NavHost(navController, startDestination = OnBoardNavigationRoutes.OnBoard.name){
    NavHost(navController, startDestination = LoginSignUpNavigationRoutes.Login.name){
        onBoardDestination(onNavigateToLogin = {
            navController.navigateToLogin()
        })
        loginDestination(
            onNavigateToHome = {
                navController.navigateToHome()
            },
            onNavigateToSignUp = {
                navController.navigateToSignUp()
            },
            onPopBackStack = {
                navController.popBackStack()
            }
        )
        signUpDestination(onPopBackStack = {
            navController.popBackStack()
        })
        homeDestination()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppStart(onNavigateToLogin: () -> Unit) {

    SoshoPayTheme {

        val pagerState = rememberPagerState (pageCount = { 2 })

        HorizontalPager(state = pagerState) { currentPage ->
            when(currentPage) {
                0 -> DeviceLoansOnBoard(
                    pagerState.currentPage, pagerState, onNavigateToLogin
                )
                1 -> CashLoansOnBoard(
                    pagerState.currentPage, pagerState, onNavigateToLogin
                )
            }
        }

    }

}
