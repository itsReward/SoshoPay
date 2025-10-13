package com.soshopay.android.ui.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.soshopay.android.ui.component.loans.CashLoanApplicationScreen
import com.soshopay.android.ui.component.loans.LoanDashboardScreen
import com.soshopay.android.ui.component.loans.LoanDetailsScreen
import com.soshopay.android.ui.component.loans.LoanHistoryScreen
import com.soshopay.android.ui.component.payments.PaymentDashboardScreen
import com.soshopay.android.ui.component.payments.PaymentHistoryScreen
import com.soshopay.domain.model.Loan

/**
 * Loan Payment Routes
 * Single Responsibility: Defines all routes for loan and payment-related screens
 */
enum class LoanPaymentRoutes {
    LoanDashboard,
    CashLoanApplication,
    PayGoApplication,
    LoanHistory,
    LoanDetails,
    PaymentDashboard,
    PaymentProcessing,
    PaymentHistory,
}

/**
 * Loan Payment Navigation Graph
 *
 * This navigation graph handles all loan and payment-related navigation flows.
 * It follows the Single Responsibility Principle by managing only loan/payment navigation.
 *
 * Navigation Flow:
 * LoanDashboard -> CashLoanApplication -> LoanHistory
 * LoanDashboard -> PayGoApplication -> LoanHistory
 * LoanDashboard -> PaymentDashboard
 *
 * @param navController The NavController for navigation
 */
fun NavGraphBuilder.loanPaymentNavGraph(navController: NavController) {
    navigation(
        route = "LoanPaymentRoot",
        startDestination = LoanPaymentRoutes.LoanDashboard.name,
    ) {
        // Loan Dashboard - Entry point for loan and payment operations
        loanDashboardDestination(
            onNavigateToCashLoan = {
                navController.navigateToCashLoanApplication()
            },
            onNavigateToPayGo = {
                navController.navigateToPayGoApplication()
            },
            onNavigateToLoanHistory = {
                navController.navigateToLoanHistory()
            },
            onNavigateToPayments = {
                navController.navigateToPaymentDashboard()
            },
            onNavigateToProfile = {
                // Profile navigation to be handled by parent
                navController.popBackStack()
            },
        )

        // Cash Loan Application
        cashLoanApplicationDestination(
            onNavigateToLoanHistory = {
                navController.navigateToLoanHistory()
            },
            onNavigateBack = {
                navController.popBackStack()
            },
        )

        // PayGo Application
        payGoApplicationDestination(
            onNavigateToLoanHistory = {
                navController.navigateToLoanHistory()
            },
            onNavigateBack = {
                navController.popBackStack()
            },
        )

        // Loan History
        loanHistoryDestination(
            onNavigateToLoanDetails = { loanId ->
                navController.navigateToLoanDetails(loanId)
            },
            onNavigateBack = {
                navController.popBackStack()
            },
        )

        // Loan Details
        loanDetailsDestination(
            onNavigateToPayment = { loan ->
                navController.navigateToPaymentDashboard()
            },
            onNavigateBack = {
                navController.popBackStack()
            },
        )

        // Payment Dashboard
        paymentDashboardDestination(
            onNavigateToPaymentProcessing = { loan ->
                navController.navigateToPaymentProcessing()
            },
            onNavigateToPaymentHistory = {
                navController.navigateToPaymentHistory()
            },
        )

        // Payment Processing
        paymentProcessingDestination(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPaymentHistory = {
                navController.navigateToPaymentHistory()
            },
        )

        // Payment History
        paymentHistoryDestination(
            onNavigateBack = {
                navController.popBackStack()
            },
        )
    }
}

// ============================================================================
// DESTINATION COMPOSABLES - Interface Segregation Principle
// Each destination is defined separately with clear responsibilities
// ============================================================================

/**
 * Loan Dashboard Destination
 * Entry point for loan and payment operations
 */
fun NavGraphBuilder.loanDashboardDestination(
    onNavigateToCashLoan: () -> Unit,
    onNavigateToPayGo: () -> Unit,
    onNavigateToLoanHistory: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    composable(LoanPaymentRoutes.LoanDashboard.name) {
        LoanDashboardScreen(
            onNavigateToCashLoan = onNavigateToCashLoan,
            onNavigateToPayGo = onNavigateToPayGo,
            onNavigateToLoanHistory = onNavigateToLoanHistory,
            onNavigateToPayments = onNavigateToPayments,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}

/**
 * Cash Loan Application Destination
 * Handles cash loan application flow
 */
fun NavGraphBuilder.cashLoanApplicationDestination(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(route = LoanPaymentRoutes.CashLoanApplication.name) {
        CashLoanApplicationScreen(
            onNavigateToLoanHistory = onNavigateToLoanHistory,
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * PayGo Application Destination
 * Handles PayGo loan application flow
 */
fun NavGraphBuilder.payGoApplicationDestination(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(LoanPaymentRoutes.PayGoApplication.name) {
        // PayGoApplicationScreen will be implemented
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Loan History Destination
 * Displays user's loan history with filtering
 */
fun NavGraphBuilder.loanHistoryDestination(
    onNavigateToLoanDetails: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(LoanPaymentRoutes.LoanHistory.name) {
        LoanHistoryScreen(
            onNavigateToLoanDetails = onNavigateToLoanDetails,
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Loan Details Destination
 * Shows detailed information about a specific loan
 */
fun NavGraphBuilder.loanDetailsDestination(
    onNavigateToPayment: (Loan) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(
        route = "${LoanPaymentRoutes.LoanDetails.name}/{loanId}",
        arguments =
            listOf(
                navArgument("loanId") { type = NavType.StringType },
            ),
    ) { backStackEntry ->
        val loanId = backStackEntry.arguments?.getString("loanId") ?: ""

        LoanDetailsScreen(
            loanId = loanId,
            onNavigateToPayment = onNavigateToPayment,
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Payment Dashboard Destination
 * Entry point for payment operations
 */
fun NavGraphBuilder.paymentDashboardDestination(
    onNavigateToPaymentProcessing: (Loan) -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
) {
    composable(LoanPaymentRoutes.PaymentDashboard.name) {
        PaymentDashboardScreen(
            onNavigateToPaymentProcessing = onNavigateToPaymentProcessing,
            onNavigateToPaymentHistory = onNavigateToPaymentHistory,
        )
    }
}

/**
 * Payment Processing Destination
 * Handles payment processing flow
 */
fun NavGraphBuilder.paymentProcessingDestination(
    onNavigateBack: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
) {
    composable(LoanPaymentRoutes.PaymentProcessing.name) {
        // PaymentProcessingScreen will be implemented
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Payment History Destination
 * Displays user's payment history
 */
fun NavGraphBuilder.paymentHistoryDestination(onNavigateBack: () -> Unit) {
    composable(LoanPaymentRoutes.PaymentHistory.name) {
        PaymentHistoryScreen(onNavigateBack = onNavigateBack)
    }
}

// ============================================================================
// NAVIGATION EXTENSION FUNCTIONS - Dependency Inversion Principle
// High-level navigation logic depends on abstractions (NavController extensions)
// ============================================================================

/**
 * Navigate to Loan Dashboard
 */
fun NavController.navigateToLoanDashboard() {
    navigate(LoanPaymentRoutes.LoanDashboard.name)
}

/**
 * Navigate to Cash Loan Application
 */
fun NavController.navigateToCashLoanApplication() {
    navigate(LoanPaymentRoutes.CashLoanApplication.name)
}

/**
 * Navigate to PayGo Application
 */
fun NavController.navigateToPayGoApplication() {
    navigate(LoanPaymentRoutes.PayGoApplication.name)
}

/**
 * Navigate to Loan History
 */
fun NavController.navigateToLoanHistory() {
    navigate(LoanPaymentRoutes.LoanHistory.name)
}

/**
 * Navigate to Loan Details with loan ID
 */
fun NavController.navigateToLoanDetails(loanId: String) {
    navigate("${LoanPaymentRoutes.LoanDetails.name}/$loanId")
}

/**
 * Navigate to Payment Dashboard
 */
fun NavController.navigateToPaymentDashboard() {
    navigate(LoanPaymentRoutes.PaymentDashboard.name)
}

/**
 * Navigate to Payment Processing
 */
fun NavController.navigateToPaymentProcessing() {
    navigate(LoanPaymentRoutes.PaymentProcessing.name)
}

/**
 * Navigate to Payment History
 */
fun NavController.navigateToPaymentHistory() {
    navigate(LoanPaymentRoutes.PaymentHistory.name)
}
