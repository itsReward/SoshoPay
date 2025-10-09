package com.soshopay.android.ui.component.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.iotapp.uiplayground.HomeMenu
import com.soshopay.android.ui.component.admin.Settings
import com.soshopay.android.ui.component.calculator.LoanCalculator
import com.soshopay.android.ui.component.calculator.LoanCalculatorMenu
import com.soshopay.android.ui.component.loanApplication.LoanApplication
import com.soshopay.android.ui.component.loans.LoanDashboardScreen
import com.soshopay.android.ui.component.loans.LoanDetails
import com.soshopay.android.ui.component.loans.LoansListHome
import com.soshopay.android.ui.component.notifications.Notifications
import com.soshopay.android.ui.component.payments.LoanPayments
import com.soshopay.android.ui.state.LoanPaymentNavigation

enum class HomeNavigationRoutes {
    Home,
    Calculators,
    Alerts,
    Admin,
    LoansList,
    LoanPayments,
    LoanApplicationGraph,
    LoanCalculatorGraph,
}

enum class LoanDetails {
    LoanDetails,
}

enum class LoanApplicationRoutes {
    LoanApplication,
}

enum class LoanCalculatorRoutes {
    LoanCalculator,
}

fun NavGraphBuilder.home(
    navigateToLoansList: () -> Unit,
    navigateToLoanApplication: () -> Unit,
    navigateToPaymentsList: () -> Unit,
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToAdmin: () -> Unit,
) {
    composable(LoginSignUpNavigationRoutes.Home.name) {
        HomeMenu(
            navigateToLoansList,
            navigateToLoanApplication,
            navigateToPaymentsList,
            navigateToDeviceLoanCalculator,
            navigateToCashLoanCalculator,
            navigateToNotifications,
            navigateToAdmin,
        )
    }
}

fun NavGraphBuilder.loans(
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    composable(HomeNavigationRoutes.LoansList.name) {
        LoanDashboardScreen(
            navigateToLoansDetails,
            navigateToPayments,
            navigateToPayments,
            navigateToPayments,
            navigateToPayments,
        )
    }
}

fun NavGraphBuilder.calculator(
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    onPop: () -> Unit,
) {
    composable(HomeNavigationRoutes.Calculators.name) {
        LoanCalculatorMenu(navigateToDeviceLoanCalculator, navigateToCashLoanCalculator, onPop)
    }
}

fun NavGraphBuilder.loanPayments(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.LoanPayments.name) {
        LoanPayments(onPop)
    }
}

fun NavGraphBuilder.notifications(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.Alerts.name) {
        Notifications(onPop)
    }
}

fun NavGraphBuilder.admin(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.Admin.name) {
        Settings(onPop)
    }
}

fun NavGraphBuilder.userLoansNavGraph(
    navigateToLoanDetails: () -> Unit,
    navigateToLoanApplication: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    navigation(
        route = "LoanDetailsRoot",
        startDestination = HomeNavigationRoutes.LoansList.name,
    ) {
        loans(navigateToLoanDetails, navigateToPayments, onPop)
        loanDetails(onPop)
    }
}

fun NavGraphBuilder.loanDetails(onPop: () -> Unit) {
    composable(LoanDetails.LoanDetails.name) {
        LoanDetails(onPop)
    }
}

fun NavController.navigateToLoansList() {
    navigate(HomeNavigationRoutes.LoansList.name)
}

fun NavController.navigateToPaymentsList() {
    navigate(HomeNavigationRoutes.LoanPayments.name)
}

fun NavController.navigateToLoanDetails() {
    navigate(LoanDetails.LoanDetails.name)
}

fun NavController.navigateToNotifications() {
    navigate(HomeNavigationRoutes.Alerts.name)
}

fun NavController.navigateToAdmin() {
    navigate(HomeNavigationRoutes.Admin.name)
}

fun NavController.onPop() {
    popBackStack()
}

/*
*
*
*  LOAN APPLICATION
*
*
* */
fun NavGraphBuilder.loanApplicationNavGraph(navController: NavController) {
    navigation(
        route = "LoanApplicationRoot",
        startDestination = HomeNavigationRoutes.LoanApplicationGraph.name,
    ) {
        loanApplication(navController)
    }
}

fun NavGraphBuilder.loanApplication(navController: NavController) {
    composable(
        LoanApplicationRoutes.LoanApplication.name,
    ) {
        LoanApplication { navController.popBackStack() }
    }
}

fun NavController.navigateToLoanApplication() {
    navigate(LoanApplicationRoutes.LoanApplication.name)
}

/*
*
*
*   Loan Calculator
*
* */

fun NavGraphBuilder.loanCalculatorNavGraph(
    calculatorType: String,
    navigateToLoanApplication: () -> Unit,
    onPop: () -> Unit,
) {
    navigation(
        route = "LoanCalculatorRoot",
        startDestination = HomeNavigationRoutes.LoanCalculatorGraph.name,
    ) {
        loanCalculator(calculatorType, navigateToLoanApplication, onPop)
    }
}

fun NavGraphBuilder.loanCalculator(
    calculator: String,
    navigateToLoanApplication: () -> Unit,
    onPop: () -> Unit,
) {
    composable(
        LoanCalculatorRoutes.LoanCalculator.name + "/{calculatorType}",
        arguments = listOf(navArgument("calculatorType") { type = NavType.StringType }),
    ) { backStackEntry ->
        LoanCalculator(
            backStackEntry.arguments?.getString("calculatorType")!!,
            navigateToLoanApplication,
            onPop,
        )
    }
}

fun NavController.navigateToDeviceLoanCalculator() {
    navigate(LoanCalculatorRoutes.LoanCalculator.name + "/Device")
}

fun NavController.navigateToCashLoanCalculator() {
    navigate(LoanCalculatorRoutes.LoanCalculator.name + "/Cash")
}
