package com.soshopay.android.ui.state

import com.soshopay.domain.model.CashLoanApplicationStep
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.Guarantor
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.PayGoLoanTerms
import com.soshopay.domain.model.PayGoProduct
import com.soshopay.domain.model.PayGoStep
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentSchedule
import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.model.PaymentSummary

/**
 * Sealed class representing different loan/payment UI states
 */
sealed class LoanPaymentUiState {
    object Idle : LoanPaymentUiState()

    object Loading : LoanPaymentUiState()

    data class Success<T>(
        val data: T,
    ) : LoanPaymentUiState()

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : LoanPaymentUiState()
}

/**
 * Data class representing the loan dashboard screen state
 */
data class LoanDashboardState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cashLoanFormData: CashLoanFormData? = null,
    val payGoCategories: List<String> = emptyList(),
    val isAccountComplete: Boolean = true,
    val showProfileIncompleteDialog: Boolean = false,
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Data class representing the PayGo loan application screen state
 */
data class PayGoApplicationState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val products: List<PayGoProduct> = emptyList(),
    val selectedProduct: PayGoProduct? = null,
    val usagePerDay: String = "",
    val repaymentPeriod: String = "",
    val salaryBand: String = "",
    val guarantorInfo: Guarantor? = null,
    val calculatedTerms: PayGoLoanTerms? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val showProductDetails: Boolean = false,
    val showTermsDialog: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val currentStep: PayGoStep = PayGoStep.CATEGORY_SELECTION,
    val isApplicationEnabled: Boolean = false,
    val acceptedTerms: Boolean = false,
) {
    fun hasErrors(): Boolean = errorMessage != null || validationErrors.isNotEmpty()

    fun canProceedToNextStep(): Boolean =
        when (currentStep) {
            PayGoStep.CATEGORY_SELECTION -> selectedCategory.isNotEmpty()
            PayGoStep.PRODUCT_SELECTION -> selectedProduct != null
            PayGoStep.APPLICATION_DETAILS -> usagePerDay.isNotEmpty() && repaymentPeriod.isNotEmpty() && salaryBand.isNotEmpty()
            PayGoStep.GUARANTOR_INFO -> guarantorInfo != null && guarantorInfo.isComplete()
            PayGoStep.TERMS_REVIEW -> calculatedTerms != null
        }
}

/**
 * Data class representing the loan history screen state
 */
data class LoanHistoryState(
    val loans: List<Loan> = emptyList(),
    val filteredLoans: List<Loan> = emptyList(),
    val selectedFilter: LoanHistoryFilter = LoanHistoryFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Enum for loan history filters
 */
enum class LoanHistoryFilter(
    val displayName: String,
) {
    ALL("All"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CURRENT("Current"),
    COMPLETED("Completed"),
}

/**
 * Data class representing the loan details screen state
 */
data class LoanDetailsState(
    val loan: Loan? = null,
    val loanDetails: LoanDetails? = null,
    val paymentSchedule: List<PaymentSchedule> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showEarlyPaymentOption: Boolean = false,
    val earlyPaymentSavings: Double = 0.0,
    val showAgreementDialog: Boolean = false,
    val agreementPdfUrl: String? = null,
) {
    fun hasErrors(): Boolean = errorMessage != null

    fun canMakeEarlyPayment(): Boolean = loan?.status == LoanStatus.ACTIVE && showEarlyPaymentOption
}

/**
 * Data class representing the payment dashboard screen state
 */
data class PaymentDashboardState(
    val paymentSummaries: List<PaymentSummary> = emptyList(),
    val totalDueAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val currentCount: Int = 0,
    val nextPaymentDate: Long? = null,
    val nextPaymentAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
) {
    fun hasErrors(): Boolean = errorMessage != null

    fun hasOverduePayments(): Boolean = overdueCount > 0

    fun hasDuePayments(): Boolean = paymentSummaries.any { it.status == PaymentStatus.OVERDUE || it.amountDue > 0 }
}

/**
 * Data class representing the payment processing screen state
 */
data class PaymentProcessingState(
    val selectedLoan: Loan? = null,
    val paymentMethods: List<PaymentMethodInfo> = emptyList(),
    val selectedPaymentMethod: PaymentMethodInfo? = null,
    val paymentAmount: String = "",
    val phoneNumber: String = "",
    val customerReference: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val showPaymentConfirmation: Boolean = false,
    val paymentProcessing: Boolean = false,
    val paymentResult: PaymentResult? = null,
    val isEarlyPayment: Boolean = false,
    val earlyPaymentSavings: Double = 0.0,
    val showPaymentMethodSelectionModal: Boolean = false,
    val showPhoneInputModal: Boolean = false,
    val showPaymentResultModal: Boolean = false,
) {
    fun hasErrors(): Boolean = errorMessage != null || validationErrors.isNotEmpty()

    fun isPaymentEnabled(): Boolean =
        selectedPaymentMethod != null &&
            paymentAmount.isNotEmpty() &&
            phoneNumber.isNotEmpty() &&
            validationErrors.isEmpty() &&
            !isLoading
}

/**
 * Data class representing payment result
 */
data class PaymentResult(
    val isSuccessful: Boolean,
    val transactionId: String?,
    val receiptNumber: String?,
    val message: String,
    val failureReason: String? = null,
)

/**
 * Data class representing the payment history screen state
 */
data class PaymentHistoryState(
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Sealed class representing different loan and payment events
 */
sealed class LoanPaymentEvent {
    // Loan Dashboard Events
    object LoadLoanDashboard : LoanPaymentEvent()

    object DismissProfileIncompleteDialog : LoanPaymentEvent()

    object NavigateToProfile : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - NAVIGATION EVENTS ==========
    object InitializeCashLoanApplication : LoanPaymentEvent()

    object LoadCashLoanDraft : LoanPaymentEvent()

    data class NavigateToStep(
        val step: CashLoanApplicationStep,
    ) : LoanPaymentEvent()

    object NextStep : LoanPaymentEvent()

    object PreviousStep : LoanPaymentEvent()

    object CancelApplication : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - STEP 1: LOAN DETAILS ==========
    data class UpdateLoanAmount(
        val amount: String,
    ) : LoanPaymentEvent()

    data class UpdateLoanPurpose(
        val purpose: String,
    ) : LoanPaymentEvent()

    data class UpdateRepaymentPeriod(
        val period: String,
    ) : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - STEP 2: INCOME & EMPLOYMENT ==========
    data class UpdateMonthlyIncome(
        val income: String,
    ) : LoanPaymentEvent()

    data class UpdateEmployerIndustry(
        val industry: String,
    ) : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - STEP 3: COLLATERAL INFORMATION ==========
    data class UpdateCollateralType(
        val type: String,
    ) : LoanPaymentEvent()

    data class UpdateCollateralValue(
        val value: String,
    ) : LoanPaymentEvent()

    data class UpdateCollateralDetails(
        val details: String,
    ) : LoanPaymentEvent()

    data class UploadCollateralDocument(
        val fileBytes: ByteArray,
        val fileName: String,
        val fileType: String,
    ) : LoanPaymentEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is UploadCollateralDocument) return false
            if (!fileBytes.contentEquals(other.fileBytes)) return false
            if (fileName != other.fileName) return false
            if (fileType != other.fileType) return false
            return true
        }

        override fun hashCode(): Int {
            var result = fileBytes.contentHashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + fileType.hashCode()
            return result
        }
    }

    data class RemoveCollateralDocument(
        val documentId: String,
    ) : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - STEP 4: TERMS REVIEW ==========
    object CalculateCashLoanTerms : LoanPaymentEvent()

    object ShowTermsDialog : LoanPaymentEvent()

    object DismissTermsDialog : LoanPaymentEvent()

    object AcceptTerms : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - STEP 5: CONFIRMATION ==========
    object ShowConfirmationDialog : LoanPaymentEvent()

    object DismissConfirmationDialog : LoanPaymentEvent()

    object SubmitCashLoanApplication : LoanPaymentEvent()

    // ========== CASH LOAN APPLICATION - DRAFT MANAGEMENT ==========
    object SaveDraft : LoanPaymentEvent()

    object ClearValidationErrors : LoanPaymentEvent()

    // Cash Loan Application Events
    object CalculateLoanTerms : LoanPaymentEvent()

    object SaveDraftApplication : LoanPaymentEvent()

    // PayGo Application Events

    object LoadPayGoCategories : LoanPaymentEvent()

    // PayGo Initialization
    object InitializePayGoApplication : LoanPaymentEvent()

    object LoadPayGoDraft : LoanPaymentEvent()

    object SavePayGoDraft : LoanPaymentEvent()

    // PayGo Step Navigation
    object NextPayGoStep : LoanPaymentEvent()

    object PreviousPayGoStep : LoanPaymentEvent()

    // Step 1: Category Selection
    data class UpdatePayGoCategory(
        val category: String,
    ) : LoanPaymentEvent()

    // Step 2: Product Selection
    data class UpdatePayGoProduct(
        val product: PayGoProduct,
    ) : LoanPaymentEvent()

    object ShowPayGoProductDetails : LoanPaymentEvent()

    object DismissPayGoProductDetails : LoanPaymentEvent()

    // Step 3: Application Details
    data class UpdatePayGoUsage(
        val usage: String,
    ) : LoanPaymentEvent()

    data class UpdatePayGoRepaymentPeriod(
        val period: String,
    ) : LoanPaymentEvent()

    data class UpdatePayGoSalaryBand(
        val band: String,
    ) : LoanPaymentEvent()

    // Step 4: Guarantor Information
    data class UpdatePayGoGuarantor(
        val guarantor: Guarantor,
    ) : LoanPaymentEvent()

    // Step 5: Terms Review
    object CalculatePayGoTerms : LoanPaymentEvent()

    data class UpdatePayGoTermsAcceptance(
        val accepted: Boolean,
    ) : LoanPaymentEvent()

    object ShowPayGoTermsDialog : LoanPaymentEvent()

    object DismissPayGoTermsDialog : LoanPaymentEvent()

    // Final Submission
    object ShowPayGoConfirmationDialog : LoanPaymentEvent()

    object DismissPayGoConfirmationDialog : LoanPaymentEvent()

    object SubmitPayGoApplication : LoanPaymentEvent()

    object ConfirmPayGoSubmission : LoanPaymentEvent()

    // Validation
    object ClearPayGoValidationErrors : LoanPaymentEvent()

    data class SelectCategory(
        val category: String,
    ) : LoanPaymentEvent()

    data class LoadCategoryProducts(
        val categoryId: String,
    ) : LoanPaymentEvent()

    data class SelectProduct(
        val product: PayGoProduct,
    ) : LoanPaymentEvent()

    object ShowProductDetails : LoanPaymentEvent()

    object DismissProductDetails : LoanPaymentEvent()

    data class UpdateUsagePerDay(
        val usage: String,
    ) : LoanPaymentEvent()

    data class UpdateSalaryBand(
        val band: String,
    ) : LoanPaymentEvent()

    data class UpdateGuarantorInfo(
        val guarantor: Guarantor,
    ) : LoanPaymentEvent()

    // Loan History Events
    object LoadLoanHistory : LoanPaymentEvent()

    object RefreshLoanHistory : LoanPaymentEvent()

    data class ApplyLoanFilter(
        val filter: LoanHistoryFilter,
    ) : LoanPaymentEvent()

    data class SelectLoan(
        val loanId: String,
    ) : LoanPaymentEvent()

    // Loan Details Events
    data class LoadLoanDetails(
        val loanId: String,
    ) : LoanPaymentEvent()

    object ShowEarlyPaymentOption : LoanPaymentEvent()

    object HideEarlyPaymentOption : LoanPaymentEvent()

    object ShowLoanAgreement : LoanPaymentEvent()

    object DismissLoanAgreement : LoanPaymentEvent()

    data class DownloadLoanAgreement(
        val loanId: String,
    ) : LoanPaymentEvent()

    // Payment Dashboard Events
    object LoadPaymentDashboard : LoanPaymentEvent()

    object RefreshPaymentDashboard : LoanPaymentEvent()

    data class SelectLoanForPayment(
        val loan: Loan,
    ) : LoanPaymentEvent()

    // Payment Processing Events
    object LoadPaymentMethods : LoanPaymentEvent()

    data class SelectPaymentMethod(
        val method: PaymentMethodInfo,
    ) : LoanPaymentEvent()

    data class UpdatePaymentAmount(
        val amount: String,
    ) : LoanPaymentEvent()

    data class UpdatePhoneNumber(
        val phoneNumber: String,
    ) : LoanPaymentEvent()

    data class UpdateCustomerReference(
        val reference: String,
    ) : LoanPaymentEvent()

    object ShowPaymentConfirmation : LoanPaymentEvent()

    object DismissPaymentConfirmation : LoanPaymentEvent()

    object ProcessPayment : LoanPaymentEvent()

    data class ProcessEarlyPayment(
        val loanId: String,
        val amount: Double,
    ) : LoanPaymentEvent()

    object ShowPaymentMethodSelection : LoanPaymentEvent()

    object DismissPaymentMethodSelection : LoanPaymentEvent()

    object ConfirmPaymentMethodSelection : LoanPaymentEvent()

    object ShowPhoneInput : LoanPaymentEvent()

    object DismissPhoneInput : LoanPaymentEvent()

    object DismissPaymentResult : LoanPaymentEvent()

    object ViewReceipt : LoanPaymentEvent()

    object ReturnToDashboard : LoanPaymentEvent()

    // Payment History Events
    object LoadPaymentHistory : LoanPaymentEvent()

    object RefreshPaymentHistory : LoanPaymentEvent()

    object LoadMorePaymentHistory : LoanPaymentEvent()

    data class DownloadReceipt(
        val receiptNumber: String,
    ) : LoanPaymentEvent()

    // Common Events
    object ClearError : LoanPaymentEvent()

    object NavigateBack : LoanPaymentEvent()
}

/**
 * Sealed class representing different loan and payment navigation destinations
 */
sealed class LoanPaymentNavigation {
    object ToLoanDashboard : LoanPaymentNavigation()

    object ToCashLoanApplication : LoanPaymentNavigation()

    object ToPayGoApplication : LoanPaymentNavigation()

    object ToLoanHistory : LoanPaymentNavigation()

    data class ToLoanDetails(
        val loanId: String,
    ) : LoanPaymentNavigation()

    object ToPaymentDashboard : LoanPaymentNavigation()

    data class ToPaymentProcessing(
        val loan: Loan,
    ) : LoanPaymentNavigation()

    object ToPaymentHistory : LoanPaymentNavigation()

    object ToProfile : LoanPaymentNavigation()

    object Back : LoanPaymentNavigation()
}
