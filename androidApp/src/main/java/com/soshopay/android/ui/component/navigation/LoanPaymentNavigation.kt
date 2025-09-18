import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.soshopay.android.ui.component.loans.CashLoanApplicationScreen
import com.soshopay.android.ui.component.loans.LoanDashboardScreen
import com.soshopay.android.ui.component.payments.PaymentDashboardScreen
import com.soshopay.domain.model.Loan

/**
 * PayGo Application destination
 */
fun NavGraphBuilder.payGoApplicationDestination(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(LoanPaymentRoutes.PayGoApplication.name) {
        // PayGoApplicationScreen will be implemented next
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Loan History destination
 */
fun NavGraphBuilder.loanHistoryDestination(
    onNavigateToLoanDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(LoanPaymentRoutes.LoanHistory.name) {
        // LoanHistoryScreen will be implemented next
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Loan Details destination with loanId parameter
 */
fun NavGraphBuilder.loanDetailsDestination(
    onNavigateToPayment: (Loan) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "${LoanPaymentRoutes.LoanDetails.name}/{loanId}",
        arguments = listOf(
            navArgument("loanId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
        // LoanDetailsScreen will be implemented next
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Payment Dashboard destination
 */
fun NavGraphBuilder.paymentDashboardDestination(
    onNavigateToPaymentProcessing: (Loan) -> Unit,
    onNavigateToPaymentHistory: () -> Unit
) {
    composable(LoanPaymentRoutes.PaymentDashboard.name) {
        PaymentDashboardScreen(
            onNavigateToPaymentProcessing = onNavigateToPaymentProcessing,
            onNavigateToPaymentHistory = onNavigateToPaymentHistory
        )
    }
}

/**
 * Payment Processing destination
 */
fun NavGraphBuilder.paymentProcessingDestination(
    onNavigateBack: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit
) {
    composable(LoanPaymentRoutes.PaymentProcessing.name) {
        // PaymentProcessingScreen will be implemented next
        // For now, navigate back
        onNavigateBack()
    }
}

/**
 * Payment History destination
 */
fun NavGraphBuilder.paymentHistoryDestination(
    onNavigateBack: () -> Unit
) {
    composable(LoanPaymentRoutes.PaymentHistory.name) {
        // PaymentHistoryScreen will be implemented next
        // For now, navigate back
        onNavigateBack()
    }
}

// Navigation extension functions for NavController

fun NavController.navigateToLoanDashboard() {
    navigate(LoanPaymentRoutes.LoanDashboard.name)
}

fun NavController.navigateToCashLoanApplication() {
    navigate(LoanPaymentRoutes.CashLoanApplication.name)
}

fun NavController.navigateToPayGoApplication() {
    navigate(LoanPaymentRoutes.PayGoApplication.name)
}

fun NavController.navigateToLoanHistory() {
    navigate(LoanPaymentRoutes.LoanHistory.name)
}

fun NavController.navigateToLoanDetails(loanId: String) {
    navigate("${LoanPaymentRoutes.LoanDetails.name}/$loanId")
}

fun NavController.navigateToPaymentDashboard() {
    navigate(LoanPaymentRoutes.PaymentDashboard.name)
}

fun NavController.navigateToPaymentProcessing() {
    navigate(LoanPaymentRoutes.PaymentProcessing.name)
}

fun NavController.navigateToPaymentHistory() {
    navigate(LoanPaymentRoutes.PaymentHistory.name)
}


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
 * Loan Dashboard destination
 */
fun NavGraphBuilder.loanDashboardDestination(
    onNavigateToCashLoan: () -> Unit,
    onNavigateToPayGo: () -> Unit,
    onNavigateToLoanHistory: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    composable(LoanPaymentRoutes.LoanDashboard.name) {
        LoanDashboardScreen(
            onNavigateToCashLoan = onNavigateToCashLoan,
            onNavigateToPayGo = onNavigateToPayGo,
            onNavigateToLoanHistory = onNavigateToLoanHistory,
            onNavigateToPayments = onNavigateToPayments,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}

/**
 * Cash Loan Application destination
 */
fun NavGraphBuilder.cashLoanApplicationDestination(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(LoanPaymentRoutes.CashLoanApplication.name) {
        CashLoanApplicationScreen(
            onNavigateToLoanHistory = onNavigateToLoanHistory,
            onNavigateBack = onNavigateBack
        )
    }
}