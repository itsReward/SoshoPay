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
import com.soshopay.android.ui.component.loans.CashLoanApplicationScreen
import com.soshopay.android.ui.component.loans.LoanDashboardScreen
import com.soshopay.android.ui.component.loans.LoanDetails
import com.soshopay.android.ui.component.notifications.Notifications
import com.soshopay.android.ui.component.payments.LoanPayments
import com.soshopay.android.ui.component.payments.PaymentDashboardScreen
import com.soshopay.android.ui.component.profile.ProfileScreen

/**
 * Home Navigation Routes
 * Defines primary navigation destinations in the home screen
 */
enum class HomeNavigationRoutes {
    Home,
    Calculators,
    Alerts,
    Admin,
    LoansList,
    LoanPayments,
    LoanApplicationGraph,
    LoanCalculatorGraph,
    Profile,
}

/**
 * Loan Details Navigation
 */
enum class LoanDetails {
    LoanDetails,
}

/**
 * Loan Application Routes
 */
enum class LoanApplicationRoutes {
    LoanApplication,
    CashLoanApplication,
    PayGoApplication,
}

/**
 * Loan Calculator Routes
 */
enum class LoanCalculatorRoutes {
    LoanCalculator,
}

// ============================================================================
// HOME DESTINATION - Entry point for the application after authentication
// ============================================================================

/**
 * Home Destination
 * Main entry point after user authentication
 */
fun NavGraphBuilder.home(
    navigateToLoansList: () -> Unit,
    navigateToLoanApplication: () -> Unit,
    navigateToPaymentsList: () -> Unit,
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToAdmin: () -> Unit,
    navigateToProfile: () -> Unit,
) {
    composable(AuthNavigationRoutes.Home.name) {
        HomeMenu(
            navigateToLoansList,
            navigateToLoanApplication,
            navigateToPaymentsList,
            navigateToDeviceLoanCalculator,
            navigateToCashLoanCalculator,
            navigateToNotifications,
            navigateToAdmin,
            navigateToProfile,
        )
    }
}

// ============================================================================
// LOAN DESTINATIONS
// ============================================================================

/**
 * Loans Destination
 * Displays loan dashboard with options for cash loans and PayGo
 */
fun NavGraphBuilder.loans(
    navigateToLoansDetails: () -> Unit,
    navigateToPayGoApplication: () -> Unit,
    navigateToCashLoanApplication: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    composable(HomeNavigationRoutes.LoansList.name) {
        LoanDashboardScreen(
            onNavigateToCashLoan = navigateToCashLoanApplication,
            onNavigateToPayGo = navigateToPayGoApplication,
            onNavigateToLoanHistory = navigateToPayments,
            onNavigateToPayments = navigateToPayments,
            onNavigateToProfile = navigateToPayments,
        )
    }
}

/**
 * Loan Applications Destination
 * Handles loan application screens
 */
fun NavGraphBuilder.loanApplications(
    navigateToLoanHistory: () -> Unit,
    onPop: () -> Unit,
) {
    composable(HomeNavigationRoutes.LoanApplicationGraph.name) {
        CashLoanApplicationScreen(
            navigateToLoanHistory,
            onPop,
        )
    }
}

/**
 * Loan Details Destination
 * Shows detailed information about a specific loan
 */
fun NavGraphBuilder.loanDetails(onPop: () -> Unit) {
    composable(LoanDetails.LoanDetails.name) {
        LoanDetails(onPop)
    }
}

// ============================================================================
// USER LOANS NAVIGATION GRAPH
// ============================================================================

/**
 * User Loans Navigation Graph
 * Handles all loan-related navigation within a nested graph
 */
fun NavGraphBuilder.userLoansNavGraph(
    navigateToLoanDetails: () -> Unit,
    navigateToCashLoanApplication: () -> Unit,
    navigateToPayGoApplication: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    navigation(
        route = "LoanDetailsRoot",
        startDestination = HomeNavigationRoutes.LoansList.name,
    ) {
        loans(
            navigateToLoansDetails = navigateToLoanDetails,
            navigateToPayGoApplication = navigateToPayGoApplication,
            navigateToCashLoanApplication = navigateToCashLoanApplication,
            onPop = onPop,
            navigateToPayments = navigateToPayments,
        )
        loanDetails(onPop)
    }
}

// ============================================================================
// CALCULATOR DESTINATIONS
// ============================================================================

/**
 * Calculator Menu Destination
 * Entry point for loan calculators
 */
fun NavGraphBuilder.calculator(
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    onPop: () -> Unit,
) {
    composable(HomeNavigationRoutes.Calculators.name) {
        LoanCalculatorMenu(navigateToDeviceLoanCalculator, navigateToCashLoanCalculator, onPop)
    }
}

/**
 * Loan Calculator Navigation Graph
 * Handles calculator-specific navigation
 */
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

/**
 * Loan Calculator Destination
 * Displays specific loan calculator (Cash or Device)
 */
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

// ============================================================================
// LOAN APPLICATION NAVIGATION GRAPH
// ============================================================================

/**
 * Loan Application Navigation Graph
 * Handles loan application flow
 */
fun NavGraphBuilder.loanApplicationNavGraph(navController: NavController) {
    navigation(
        route = "LoanApplicationRoot",
        startDestination = HomeNavigationRoutes.LoanApplicationGraph.name,
    ) {
        loanApplication(navController)
    }
}

/**
 * Loan Application Destination
 */
fun NavGraphBuilder.loanApplication(navController: NavController) {
    composable(
        LoanApplicationRoutes.LoanApplication.name,
    ) {
        LoanApplication { navController.popBackStack() }
    }
}

// ============================================================================
// PROFILE DESTINATION - Add this function
// ============================================================================

/**
 * Profile Destination
 */
fun NavGraphBuilder.profile(
    onPop: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable(HomeNavigationRoutes.Profile.name) {
        ProfileScreen(
            onNavigateBack = onPop,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}

// ============================================================================
// OTHER DESTINATIONS
// ============================================================================

/**
 * Loan Payments Destination
 */
fun NavGraphBuilder.loanPayments(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.LoanPayments.name) {
        PaymentDashboardScreen(
            onNavigateToPaymentProcessing = { /* Handle payment processing navigation */ },
            onNavigateToPaymentHistory = { },
        )
    }
}

/**
 * Notifications Destination
 */
fun NavGraphBuilder.notifications(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.Alerts.name) {
        Notifications(onPop)
    }
}

/**
 * Admin/Settings Destination
 */
fun NavGraphBuilder.admin(onPop: () -> Unit) {
    composable(HomeNavigationRoutes.Admin.name) {
        Settings(onPop)
    }
}

// ============================================================================
// NAVIGATION EXTENSION FUNCTIONS - NavController Extensions
// ============================================================================

/**
 * Navigate to Profile
 */
fun NavController.navigateToProfile() {
    navigate(HomeNavigationRoutes.Profile.name)
}

/**
 * Navigate to Loans List
 */
fun NavController.navigateToLoansList() {
    navigate(HomeNavigationRoutes.LoansList.name)
}

/**
 * Navigate to Payments List
 */
fun NavController.navigateToPaymentsList() {
    navigate(HomeNavigationRoutes.LoanPayments.name)
}

/**
 * Navigate to Loan Details
 */
fun NavController.navigateToLoanDetails() {
    navigate(LoanApplicationRoutes.CashLoanApplication.name)
}

/**
 * Navigate to Notifications
 */
fun NavController.navigateToNotifications() {
    navigate(HomeNavigationRoutes.Alerts.name)
}

/**
 * Navigate to Admin
 */
fun NavController.navigateToAdmin() {
    navigate(HomeNavigationRoutes.Admin.name)
}

/**
 * Navigate to Loan Application
 */
fun NavController.navigateToLoanApplication() {
    navigate(LoanApplicationRoutes.LoanApplication.name)
}

/**
 * Navigate to Device Loan Calculator
 */
fun NavController.navigateToDeviceLoanCalculator() {
    navigate(LoanCalculatorRoutes.LoanCalculator.name + "/Device")
}

/**
 * Navigate to Cash Loan Calculator
 */
fun NavController.navigateToCashLoanCalculator() {
    navigate(LoanCalculatorRoutes.LoanCalculator.name + "/Cash")
}

/**
 * Pop back stack
 */
fun NavController.onPop() {
    popBackStack()
}
