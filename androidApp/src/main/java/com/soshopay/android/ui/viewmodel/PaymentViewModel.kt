package com.soshopay.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.state.PaymentDashboardState
import com.soshopay.android.ui.state.PaymentHistoryState
import com.soshopay.android.ui.state.PaymentProcessingState
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.usecase.payment.CalculateEarlyPayoffUseCase
import com.soshopay.domain.usecase.payment.DownloadReceiptUseCase
import com.soshopay.domain.usecase.payment.GetPaymentDashboardUseCase
import com.soshopay.domain.usecase.payment.GetPaymentHistoryUseCase
import com.soshopay.domain.usecase.payment.GetPaymentMethodsUseCase
import com.soshopay.domain.usecase.payment.GetPaymentStatusUseCase
import com.soshopay.domain.usecase.payment.ProcessPaymentUseCase
import com.soshopay.domain.util.SoshoPayException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Comprehensive ViewModel for payment operations following MVVM and Clean Architecture patterns.
 *
 * This ViewModel coordinates all payment workflows including dashboard, processing, history,
 * and EcoCash integration. It depends only on Use Cases (Domain layer) and manages UI state reactively.
 *
 * Key principles followed:
 * - Single Responsibility: Each method handles one specific payment operation
 * - Dependency Inversion: Depends on Use Case abstractions, not implementations
 * - Open/Closed: Extensible for new payment features without modifying existing code
 * - Interface Segregation: Uses focused Use Cases rather than monolithic services
 *
 * @param getPaymentDashboardUseCase Use case for retrieving payment dashboard data
 * @param getPaymentMethodsUseCase Use case for getting available payment methods
 * @param processPaymentUseCase Use case for processing payments
 * @param getPaymentHistoryUseCase Use case for retrieving payment history
 * @param getPaymentStatusUseCase Use case for checking payment status
 * @param downloadReceiptUseCase Use case for downloading payment receipts
 * @param calculateEarlyPayoffUseCase Use case for calculating early payoff amounts
 */
class PaymentViewModel(
    private val getPaymentDashboardUseCase: GetPaymentDashboardUseCase,
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val getPaymentHistoryUseCase: GetPaymentHistoryUseCase,
    private val getPaymentStatusUseCase: GetPaymentStatusUseCase,
    private val downloadReceiptUseCase: DownloadReceiptUseCase,
    private val calculateEarlyPayoffUseCase: CalculateEarlyPayoffUseCase,
) : ViewModel() {
    // ========== STATE MANAGEMENT ==========

    private val _paymentDashboardState = MutableStateFlow(PaymentDashboardState())
    val paymentDashboardState: StateFlow<PaymentDashboardState> = _paymentDashboardState.asStateFlow()

    private val _paymentProcessingState = MutableStateFlow(PaymentProcessingState())
    val paymentProcessingState: StateFlow<PaymentProcessingState> = _paymentProcessingState.asStateFlow()

    private val _paymentHistoryState = MutableStateFlow(PaymentHistoryState())
    val paymentHistoryState: StateFlow<PaymentHistoryState> = _paymentHistoryState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LoanPaymentNavigation>()
    val navigationEvents: SharedFlow<LoanPaymentNavigation> = _navigationEvents.asSharedFlow()

    // ========== EVENT HANDLING ==========

    fun onEvent(event: LoanPaymentEvent) {
        when (event) {
            // Payment Dashboard Events
            is LoanPaymentEvent.LoadPaymentDashboard -> loadPaymentDashboard()
            is LoanPaymentEvent.RefreshPaymentDashboard -> refreshPaymentDashboard()
            is LoanPaymentEvent.SelectLoanForPayment -> selectLoanForPayment(event.loan)

            // Payment Processing Events
            is LoanPaymentEvent.LoadPaymentMethods -> loadPaymentMethods()
            is LoanPaymentEvent.SelectPaymentMethod -> selectPaymentMethod(event.method)
            is LoanPaymentEvent.UpdatePaymentAmount -> updatePaymentAmount(event.amount)
            is LoanPaymentEvent.UpdatePhoneNumber -> updatePhoneNumber(event.phoneNumber)
            is LoanPaymentEvent.UpdateCustomerReference -> updateCustomerReference(event.reference)
            is LoanPaymentEvent.ShowPaymentConfirmation -> showPaymentConfirmation()
            is LoanPaymentEvent.DismissPaymentConfirmation -> dismissPaymentConfirmation()
            is LoanPaymentEvent.ProcessPayment -> processPayment()
            is LoanPaymentEvent.ProcessEarlyPayment -> processEarlyPayment(event.loanId, event.amount)

            // Payment History Events
            is LoanPaymentEvent.LoadPaymentHistory -> loadPaymentHistory()
            is LoanPaymentEvent.RefreshPaymentHistory -> refreshPaymentHistory()
            is LoanPaymentEvent.LoadMorePaymentHistory -> loadMorePaymentHistory()
            is LoanPaymentEvent.DownloadReceipt -> downloadReceipt(event.receiptNumber)

            // Common Events
            is LoanPaymentEvent.ClearError -> clearError()
            is LoanPaymentEvent.NavigateBack -> navigateBack()

            else -> { /* Other events handled in LoanViewModel */ }
        }
    }

    // ========== PAYMENT DASHBOARD OPERATIONS ==========

    private fun loadPaymentDashboard() {
        _paymentDashboardState.value = _paymentDashboardState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getPaymentDashboardUseCase()

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val dashboard = result.data
                    val summaries = dashboard.paymentSummaries

                    _paymentDashboardState.value =
                        _paymentDashboardState.value.copy(
                            isLoading = false,
                            paymentSummaries = summaries,
                            totalDueAmount = summaries.sumOf { it.amountDue },
                            overdueCount = summaries.count { it.isOverdue() },
                            currentCount = summaries.count { it.isCurrent() },
                            nextPaymentDate = summaries.minByOrNull { it.dueDate }?.dueDate,
                            nextPaymentAmount = summaries.minByOrNull { it.dueDate }?.amountDue ?: 0.0,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentDashboardState.value =
                        _paymentDashboardState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _paymentDashboardState.value = _paymentDashboardState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun refreshPaymentDashboard() {
        _paymentDashboardState.value = _paymentDashboardState.value.copy(isRefreshing = true)
        loadPaymentDashboard()
    }

    private fun selectLoanForPayment(loan: Loan) {
        viewModelScope.launch {
            _navigationEvents.emit(LoanPaymentNavigation.ToPaymentProcessing(loan))
        }
    }

    // ========== PAYMENT PROCESSING OPERATIONS ==========

    private fun loadPaymentMethods() {
        _paymentProcessingState.value = _paymentProcessingState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getPaymentMethodsUseCase()

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val methods = result.data
                    _paymentProcessingState.value =
                        _paymentProcessingState.value.copy(
                            isLoading = false,
                            paymentMethods = methods,
                            selectedPaymentMethod = methods.firstOrNull { it.name == "EcoCash" }, // Pre-select EcoCash
                        )

                    // Pre-populate phone number from user profile
                    prePopulateUserPhone()
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentProcessingState.value =
                        _paymentProcessingState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _paymentProcessingState.value = _paymentProcessingState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun selectPaymentMethod(method: PaymentMethodInfo) {
        _paymentProcessingState.value = _paymentProcessingState.value.copy(selectedPaymentMethod = method)
    }

    private fun updatePaymentAmount(amount: String) {
        val sanitizedAmount = amount.filter { it.isDigit() || it == '.' }
        val error = validatePaymentAmount(sanitizedAmount)
        val currentState = _paymentProcessingState.value

        _paymentProcessingState.value =
            currentState.copy(
                paymentAmount = sanitizedAmount,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("paymentAmount" to error)
                    } else {
                        currentState.validationErrors - "paymentAmount"
                    },
            )
    }

    private fun updatePhoneNumber(phoneNumber: String) {
        val error = validatePhoneNumber(phoneNumber)
        val currentState = _paymentProcessingState.value

        _paymentProcessingState.value =
            currentState.copy(
                phoneNumber = phoneNumber,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("phoneNumber" to error)
                    } else {
                        currentState.validationErrors - "phoneNumber"
                    },
            )
    }

    private fun updateCustomerReference(reference: String) {
        _paymentProcessingState.value = _paymentProcessingState.value.copy(customerReference = reference)
    }

    private fun showPaymentConfirmation() {
        _paymentProcessingState.value = _paymentProcessingState.value.copy(showPaymentConfirmation = true)
    }

    private fun dismissPaymentConfirmation() {
        _paymentProcessingState.value = _paymentProcessingState.value.copy(showPaymentConfirmation = false)
    }

    private fun processPayment() {
        val currentState = _paymentProcessingState.value
        val loan = currentState.selectedLoan ?: return
        val method = currentState.selectedPaymentMethod ?: return

        if (!currentState.isPaymentEnabled()) return

        _paymentProcessingState.value =
            currentState.copy(
                paymentProcessing = true,
                errorMessage = null,
                showPaymentConfirmation = false,
            )

        viewModelScope.launch {
            val paymentRequest =
                PaymentRequest(
                    loanId = loan.id,
                    amount = currentState.paymentAmount.toDoubleOrNull() ?: 0.0,
                    paymentMethod = method.name,
                    phoneNumber = currentState.phoneNumber,
                    customerReference = currentState.customerReference,
                )

            val result =
                processPaymentUseCase(
                    paymentRequest.loanId,
                    paymentRequest.amount,
                    paymentRequest.paymentMethod,
                    paymentRequest.phoneNumber,
                )

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val paymentId = result.data
                    // Check payment status
                    checkPaymentStatus(paymentId)
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentProcessingState.value =
                        currentState.copy(
                            paymentProcessing = false,
                            errorMessage = getErrorMessage(result.exception),
                            paymentResult =
                                com.soshopay.android.ui.state.PaymentResult(
                                    isSuccessful = false,
                                    transactionId = null,
                                    receiptNumber = null,
                                    message = getErrorMessage(result.exception),
                                    failureReason = result.exception.message,
                                ),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _paymentProcessingState.value = currentState.copy(paymentProcessing = true)
                }
            }
        }
    }

    private fun processEarlyPayment(
        loanId: String,
        amount: Double,
    ) {
        _paymentProcessingState.value =
            _paymentProcessingState.value.copy(
                paymentProcessing = true,
                errorMessage = null,
                isEarlyPayment = true,
            )

        viewModelScope.launch {
            // First calculate early payoff savings
            val earlyPayoffResult = calculateEarlyPayoffUseCase(loanId)

            when (earlyPayoffResult) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val savings = earlyPayoffResult.data
                    _paymentProcessingState.value =
                        _paymentProcessingState.value.copy(
                            earlyPaymentSavings = savings.savingsAmount,
                        )

                    // Process the early payment
                    val currentState = _paymentProcessingState.value
                    val method = currentState.selectedPaymentMethod ?: return@launch

                    val paymentRequest =
                        PaymentRequest(
                            loanId = loanId,
                            amount = amount,
                            paymentMethod = method.name,
                            phoneNumber = currentState.phoneNumber,
                            customerReference = "Early Payment - ${currentState.customerReference}",
                        )

                    val result =
                        processPaymentUseCase(
                            paymentRequest.loanId,
                            paymentRequest.amount,
                            paymentRequest.paymentMethod,
                            paymentRequest.phoneNumber,
                        )

                    when (result) {
                        is com.soshopay.domain.repository.Result.Success -> {
                            checkPaymentStatus(result.data)
                        }
                        is com.soshopay.domain.repository.Result.Error -> {
                            _paymentProcessingState.value =
                                currentState.copy(
                                    paymentProcessing = false,
                                    errorMessage = getErrorMessage(result.exception),
                                    paymentResult =
                                        com.soshopay.android.ui.state.PaymentResult(
                                            isSuccessful = false,
                                            transactionId = null,
                                            receiptNumber = null,
                                            message = getErrorMessage(result.exception),
                                            failureReason = result.exception.message,
                                        ),
                                )
                        }
                        else -> { /* Handle loading state */ }
                    }
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentProcessingState.value =
                        _paymentProcessingState.value.copy(
                            paymentProcessing = false,
                            errorMessage = getErrorMessage(earlyPayoffResult.exception),
                        )
                }
                else -> { /* Handle loading state */ }
            }
        }
    }

    private suspend fun checkPaymentStatus(paymentId: String) {
        // Simulate checking payment status (in real app, this might poll the API)
        kotlinx.coroutines.delay(3000) // Wait 3 seconds for payment processing

        val result = getPaymentStatusUseCase(paymentId)

        when (result) {
            is com.soshopay.domain.repository.Result.Success -> {
                val paymentStatus = result.data
                val isSuccessful = paymentStatus == PaymentStatus.SUCCESSFUL

                _paymentProcessingState.value =
                    _paymentProcessingState.value.copy(
                        paymentProcessing = false,
                        paymentResult =
                            com.soshopay.android.ui.state.PaymentResult(
                                isSuccessful = isSuccessful,
                                transactionId = paymentId,
                                receiptNumber = if (isSuccessful) "RCP-${System.currentTimeMillis()}" else null,
                                message = if (isSuccessful) "Payment successful!" else "Payment failed. Please try again.",
                                failureReason = if (!isSuccessful) "EcoCash transaction failed" else null,
                            ),
                    )
            }
            is com.soshopay.domain.repository.Result.Error -> {
                _paymentProcessingState.value =
                    _paymentProcessingState.value.copy(
                        paymentProcessing = false,
                        paymentResult =
                            com.soshopay.android.ui.state.PaymentResult(
                                isSuccessful = false,
                                transactionId = paymentId,
                                receiptNumber = null,
                                message = "Unable to verify payment status",
                                failureReason = result.exception.message,
                            ),
                    )
            }
            else -> { /* Handle loading state */ }
        }
    }

    private fun prePopulateUserPhone() {
        // In a real app, this would get the phone number from user profile
        // For now, we'll leave it empty to be filled by user
        _paymentProcessingState.value =
            _paymentProcessingState.value.copy(
                phoneNumber = "", // Would be populated from user profile
            )
    }

    // ========== PAYMENT HISTORY OPERATIONS ==========

    private fun loadPaymentHistory() {
        _paymentHistoryState.value = _paymentHistoryState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getPaymentHistoryUseCase(0)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val historyResponse = result.data
                    _paymentHistoryState.value =
                        _paymentHistoryState.value.copy(
                            isLoading = false,
                            payments = historyResponse.payments,
                            currentPage = historyResponse.currentPage,
                            hasMorePages = historyResponse.hasNext,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentHistoryState.value =
                        _paymentHistoryState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _paymentHistoryState.value = _paymentHistoryState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun refreshPaymentHistory() {
        _paymentHistoryState.value =
            _paymentHistoryState.value.copy(
                isRefreshing = true,
                currentPage = 0,
            )
        loadPaymentHistory()
    }

    private fun loadMorePaymentHistory() {
        val currentState = _paymentHistoryState.value
        if (!currentState.hasMorePages || currentState.isLoading) return

        viewModelScope.launch {
            val result = getPaymentHistoryUseCase(currentState.currentPage + 1)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val historyResponse = result.data
                    _paymentHistoryState.value =
                        currentState.copy(
                            payments = currentState.payments + historyResponse.payments,
                            currentPage = historyResponse.currentPage,
                            hasMorePages = historyResponse.hasNext,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentHistoryState.value =
                        currentState.copy(
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                else -> { /* Handle loading state */ }
            }
        }
    }

    private fun downloadReceipt(receiptNumber: String) {
        viewModelScope.launch {
            val result = downloadReceiptUseCase(receiptNumber)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    // In a real app, this would save the receipt to downloads folder
                    // For now, we just show a success message
                    _paymentHistoryState.value =
                        _paymentHistoryState.value.copy(
                            errorMessage = null,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _paymentHistoryState.value =
                        _paymentHistoryState.value.copy(
                            errorMessage = "Failed to download receipt: ${getErrorMessage(result.exception)}",
                        )
                }
                else -> { /* Handle loading state */ }
            }
        }
    }

    // ========== NAVIGATION OPERATIONS ==========

    private fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(LoanPaymentNavigation.Back)
        }
    }

    // ========== VALIDATION HELPERS ==========

    private fun validatePaymentAmount(amount: String): String? {
        val selectedLoan = _paymentProcessingState.value.selectedLoan
        return when {
            amount.isEmpty() -> "Payment amount is required"
            amount.toDoubleOrNull() == null -> "Invalid amount format"
            amount.toDouble() <= 0 -> "Amount must be greater than zero"
            selectedLoan != null && amount.toDouble() > selectedLoan.remainingBalance ->
                "Amount cannot exceed remaining balance"
            else -> null
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): String? =
        when {
            phoneNumber.isEmpty() -> "Phone number is required"
            phoneNumber.length < 10 -> "Phone number must be at least 10 digits"
            !phoneNumber.startsWith("07") && !phoneNumber.startsWith("+263") ->
                "Please enter a valid Zimbabwe phone number"
            else -> null
        }

    private fun clearError() {
        _paymentDashboardState.value = _paymentDashboardState.value.copy(errorMessage = null)
        _paymentProcessingState.value = _paymentProcessingState.value.copy(errorMessage = null)
        _paymentHistoryState.value = _paymentHistoryState.value.copy(errorMessage = null)
    }

    private fun getErrorMessage(throwable: Throwable): String =
        when (throwable) {
            is SoshoPayException.ValidationException -> throwable.message ?: "Validation error"
            is SoshoPayException.NetworkException -> "Network error. Please check your connection."
            is SoshoPayException.UnauthorizedException -> "Please log in again"
            else -> throwable.message ?: "An unexpected error occurred"
        }

    // ========== INITIALIZATION ==========

    init {
        loadPaymentDashboard()
    }

    // ========== PUBLIC HELPER METHODS ==========

    /**
     * Sets the selected loan for payment processing
     * Called when navigating from other screens
     */
    fun setSelectedLoan(loan: Loan) {
        _paymentProcessingState.value =
            _paymentProcessingState.value.copy(
                selectedLoan = loan,
                paymentAmount = loan.nextPaymentAmount?.toString() ?: "",
            )
        loadPaymentMethods()
    }

    /**
     * Clears the payment result to reset the state
     */
    fun clearPaymentResult() {
        _paymentProcessingState.value =
            _paymentProcessingState.value.copy(
                paymentResult = null,
                paymentProcessing = false,
                isEarlyPayment = false,
                earlyPaymentSavings = 0.0,
            )
    }
}
