package com.soshopay.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soshopay.android.ui.state.CashLoanApplicationState
import com.soshopay.android.ui.state.LoanDashboardState
import com.soshopay.android.ui.state.LoanDetailsState
import com.soshopay.android.ui.state.LoanHistoryState
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.state.PayGoApplicationState
import com.soshopay.android.ui.state.PayGoStep
import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanCalculationRequest
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.Guarantor
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType
import com.soshopay.domain.model.PayGoCalculationRequest
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoProduct
import com.soshopay.domain.usecase.loan.CalculateCashLoanTermsUseCase
import com.soshopay.domain.usecase.loan.CalculatePayGoTermsUseCase
import com.soshopay.domain.usecase.loan.DownloadLoanAgreementUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanFormDataUseCase
import com.soshopay.domain.usecase.loan.GetCurrentLoansUseCase
import com.soshopay.domain.usecase.loan.GetLoanDetailsUseCase
import com.soshopay.domain.usecase.loan.GetLoanHistoryUseCase
import com.soshopay.domain.usecase.loan.GetPayGoCategoriesUseCase
import com.soshopay.domain.usecase.loan.GetPayGoProductsUseCase
import com.soshopay.domain.usecase.loan.SaveCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.SubmitCashLoanApplicationUseCase
import com.soshopay.domain.usecase.loan.SubmitPayGoApplicationUseCase
import com.soshopay.domain.usecase.profile.GetUserProfileUseCase
import com.soshopay.domain.usecase.profile.ValidateProfileCompletionUseCase
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Comprehensive ViewModel for loan operations following MVVM and Clean Architecture patterns.
 *
 * This ViewModel coordinates all loan workflows including cash loans, PayGo loans, loan history,
 * and loan details. It depends only on Use Cases (Domain layer) and manages UI state reactively.
 *
 * Key principles followed:
 * - Single Responsibility: Each method handles one specific loan operation
 * - Dependency Inversion: Depends on Use Case abstractions, not implementations
 * - Open/Closed: Extensible for new loan features without modifying existing code
 * - Interface Segregation: Uses focused Use Cases rather than monolithic services
 *
 * @param getCashLoanFormDataUseCase Use case for retrieving cash loan form data
 * @param calculateCashLoanTermsUseCase Use case for calculating cash loan terms
 * @param submitCashLoanApplicationUseCase Use case for submitting cash loan applications
 * @param saveCashLoanDraftUseCase Use case for saving cash loan drafts
 * @param getCashLoanDraftUseCase Use case for retrieving cash loan drafts
 * @param getPayGoCategoriesUseCase Use case for getting PayGo categories
 * @param getPayGoProductsUseCase Use case for getting PayGo products
 * @param calculatePayGoTermsUseCase Use case for calculating PayGo terms
 * @param submitPayGoApplicationUseCase Use case for submitting PayGo applications
 * @param getLoanHistoryUseCase Use case for getting loan history
 * @param getLoanDetailsUseCase Use case for getting loan details
 * @param getCurrentLoansUseCase Use case for getting current loans
 * @param downloadLoanAgreementUseCase Use case for downloading loan agreements
 * @param validateProfileCompletionUseCase Use case for validating profile completion
 */
class LoanViewModel(
    private val getCashLoanFormDataUseCase: GetCashLoanFormDataUseCase,
    private val calculateCashLoanTermsUseCase: CalculateCashLoanTermsUseCase,
    private val submitCashLoanApplicationUseCase: SubmitCashLoanApplicationUseCase,
    private val saveCashLoanDraftUseCase: SaveCashLoanDraftUseCase,
    private val getCashLoanDraftUseCase: GetCashLoanDraftUseCase,
    private val getPayGoCategoriesUseCase: GetPayGoCategoriesUseCase,
    private val getPayGoProductsUseCase: GetPayGoProductsUseCase,
    private val calculatePayGoTermsUseCase: CalculatePayGoTermsUseCase,
    private val submitPayGoApplicationUseCase: SubmitPayGoApplicationUseCase,
    private val getLoanHistoryUseCase: GetLoanHistoryUseCase,
    private val getLoanDetailsUseCase: GetLoanDetailsUseCase,
    private val getCurrentLoansUseCase: GetCurrentLoansUseCase,
    private val downloadLoanAgreementUseCase: DownloadLoanAgreementUseCase,
    private val validateProfileCompletionUseCase: ValidateProfileCompletionUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {
    // ========== STATE MANAGEMENT ==========

    private val _loanDashboardState = MutableStateFlow(LoanDashboardState())
    val loanDashboardState: StateFlow<LoanDashboardState> = _loanDashboardState.asStateFlow()

    private val _cashLoanApplicationState = MutableStateFlow(CashLoanApplicationState())
    val cashLoanApplicationState: StateFlow<CashLoanApplicationState> = _cashLoanApplicationState.asStateFlow()

    private val _payGoApplicationState = MutableStateFlow(PayGoApplicationState())
    val payGoApplicationState: StateFlow<PayGoApplicationState> = _payGoApplicationState.asStateFlow()

    private val _loanHistoryState = MutableStateFlow(LoanHistoryState())
    val loanHistoryState: StateFlow<LoanHistoryState> = _loanHistoryState.asStateFlow()

    private val _loanDetailsState = MutableStateFlow(LoanDetailsState())
    val loanDetailsState: StateFlow<LoanDetailsState> = _loanDetailsState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LoanPaymentNavigation>()
    val navigationEvents: SharedFlow<LoanPaymentNavigation> = _navigationEvents.asSharedFlow()

    // ========== EVENT HANDLING ==========

    fun onEvent(event: LoanPaymentEvent) {
        when (event) {
            // Loan Dashboard Events
            is LoanPaymentEvent.LoadLoanDashboard -> loadLoanDashboard()
            is LoanPaymentEvent.DismissProfileIncompleteDialog -> dismissProfileIncompleteDialog()
            is LoanPaymentEvent.NavigateToProfile -> navigateToProfile()

            // Cash Loan Application Events
            is LoanPaymentEvent.UpdateLoanAmount -> updateLoanAmount(event.amount)
            is LoanPaymentEvent.UpdateLoanPurpose -> updateLoanPurpose(event.purpose)
            is LoanPaymentEvent.UpdateRepaymentPeriod -> updateRepaymentPeriod(event.period)
            is LoanPaymentEvent.UpdateMonthlyIncome -> updateMonthlyIncome(event.income)
            is LoanPaymentEvent.UpdateCollateralType -> updateCollateralType(event.type)
            is LoanPaymentEvent.UpdateCollateralValue -> updateCollateralValue(event.value)
            is LoanPaymentEvent.CalculateLoanTerms -> calculateCashLoanTerms()
            is LoanPaymentEvent.ShowTermsDialog -> showTermsDialog()
            is LoanPaymentEvent.DismissTermsDialog -> dismissTermsDialog()
            is LoanPaymentEvent.AcceptTerms -> acceptTerms()
            is LoanPaymentEvent.ShowConfirmationDialog -> showConfirmationDialog()
            is LoanPaymentEvent.DismissConfirmationDialog -> dismissConfirmationDialog()
            is LoanPaymentEvent.SubmitCashLoanApplication -> submitCashLoanApplication()
            is LoanPaymentEvent.SaveDraftApplication -> saveDraftApplication()

            // PayGo Application Events
            is LoanPaymentEvent.LoadPayGoCategories -> loadPayGoCategories()
            is LoanPaymentEvent.SelectCategory -> selectCategory(event.category)
            is LoanPaymentEvent.LoadCategoryProducts -> loadCategoryProducts(event.categoryId)
            is LoanPaymentEvent.SelectProduct -> selectProduct(event.product)
            is LoanPaymentEvent.ShowProductDetails -> showProductDetails()
            is LoanPaymentEvent.DismissProductDetails -> dismissProductDetails()
            is LoanPaymentEvent.UpdateUsagePerDay -> updateUsagePerDay(event.usage)
            is LoanPaymentEvent.UpdatePayGoRepaymentPeriod -> updatePayGoRepaymentPeriod(event.period)
            is LoanPaymentEvent.UpdateSalaryBand -> updateSalaryBand(event.band)
            is LoanPaymentEvent.UpdateGuarantorInfo -> updateGuarantorInfo(event.guarantor)
            is LoanPaymentEvent.NextPayGoStep -> nextPayGoStep(event.step)
            is LoanPaymentEvent.PreviousPayGoStep -> previousPayGoStep(event.step)
            is LoanPaymentEvent.CalculatePayGoTerms -> calculatePayGoTerms()
            is LoanPaymentEvent.SubmitPayGoApplication -> submitPayGoApplication()

            // Loan History Events
            is LoanPaymentEvent.LoadLoanHistory -> loadLoanHistory()
            is LoanPaymentEvent.RefreshLoanHistory -> refreshLoanHistory()
            is LoanPaymentEvent.ApplyLoanFilter -> applyLoanFilter(event.filter)
            is LoanPaymentEvent.SelectLoan -> selectLoan(event.loanId)

            // Loan Details Events
            is LoanPaymentEvent.LoadLoanDetails -> loadLoanDetails(event.loanId)
            is LoanPaymentEvent.ShowEarlyPaymentOption -> showEarlyPaymentOption()
            is LoanPaymentEvent.HideEarlyPaymentOption -> hideEarlyPaymentOption()
            is LoanPaymentEvent.ShowLoanAgreement -> showLoanAgreement()
            is LoanPaymentEvent.DismissLoanAgreement -> dismissLoanAgreement()
            is LoanPaymentEvent.DownloadLoanAgreement -> downloadLoanAgreement(event.loanId)

            // Common Events
            is LoanPaymentEvent.ClearError -> clearError()
            is LoanPaymentEvent.NavigateBack -> navigateBack()

            else -> { /* Handle other events in PaymentViewModel */ }
        }
    }

    // ========== LOAN DASHBOARD OPERATIONS ==========

    private fun loadLoanDashboard() {
        _loanDashboardState.value = _loanDashboardState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val user = getUserProfileUseCase()
            // First check if profile is complete
            when (user) {
                is Result.Loading -> {
                    _loanDashboardState.value =
                        _loanDashboardState.value.copy(
                            isLoading = true,
                        )
                }
                is Result.Error -> {
                    _loanDashboardState.value =
                        _loanDashboardState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(user.exception),
                        )
                }
                is Result.Success -> {
                    val profileValidation = validateProfileCompletionUseCase(user.data)
                    if (!profileValidation.isComplete) {
                        _loanDashboardState.value =
                            _loanDashboardState.value.copy(
                                isLoading = false,
                                isAccountComplete = false,
                                showProfileIncompleteDialog = true,
                            )
                        return@launch
                    }
                }
            }

            // Load form data and categories in parallel
            val formDataResult = getCashLoanFormDataUseCase()
            val categoriesResult = getPayGoCategoriesUseCase()

            when {
                formDataResult is com.soshopay.domain.repository.Result.Success<CashLoanFormData> &&
                    categoriesResult is com.soshopay.domain.repository.Result.Success<List<String>> -> {
                    _loanDashboardState.value =
                        _loanDashboardState.value.copy(
                            isLoading = false,
                            cashLoanFormData = formDataResult.getOrNull(),
                            payGoCategories = categoriesResult.getOrNull() ?: emptyList(),
                            isAccountComplete = true,
                        )
                }
                formDataResult is com.soshopay.domain.repository.Result.Error -> {
                    _loanDashboardState.value =
                        _loanDashboardState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(formDataResult.exception),
                        )
                }
                categoriesResult is com.soshopay.domain.repository.Result.Error -> {
                    _loanDashboardState.value =
                        _loanDashboardState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(categoriesResult.exception),
                        )
                }
            }
        }
    }

    private fun dismissProfileIncompleteDialog() {
        _loanDashboardState.value = _loanDashboardState.value.copy(showProfileIncompleteDialog = false)
    }

    private fun navigateToProfile() {
        viewModelScope.launch {
            _navigationEvents.emit(LoanPaymentNavigation.ToProfile)
        }
    }

    // ========== CASH LOAN APPLICATION OPERATIONS ==========

    private fun updateLoanAmount(amount: String) {
        val sanitizedAmount = amount.filter { it.isDigit() || it == '.' }
        val error = validateLoanAmount(sanitizedAmount)
        val currentState = _cashLoanApplicationState.value

        _cashLoanApplicationState.value =
            currentState.copy(
                loanAmount = sanitizedAmount,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("loanAmount" to error)
                    } else {
                        currentState.validationErrors - "loanAmount"
                    },
                isApplicationEnabled = currentState.isFormValid() && error == null,
            )
    }

    private fun updateLoanPurpose(purpose: String) {
        val error = validateLoanPurpose(purpose)
        val currentState = _cashLoanApplicationState.value

        _cashLoanApplicationState.value =
            currentState.copy(
                loanPurpose = purpose,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("loanPurpose" to error)
                    } else {
                        currentState.validationErrors - "loanPurpose"
                    },
            )
    }

    private fun updateRepaymentPeriod(period: String) {
        val error = validateRepaymentPeriod(period)
        val currentState = _cashLoanApplicationState.value

        _cashLoanApplicationState.value =
            currentState.copy(
                repaymentPeriod = period,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("repaymentPeriod" to error)
                    } else {
                        currentState.validationErrors - "repaymentPeriod"
                    },
            )
    }

    private fun updateMonthlyIncome(income: String) {
        val sanitizedIncome = income.filter { it.isDigit() || it == '.' }
        val error = validateMonthlyIncome(sanitizedIncome)
        val currentState = _cashLoanApplicationState.value

        _cashLoanApplicationState.value =
            currentState.copy(
                monthlyIncome = sanitizedIncome,
                validationErrors =
                    if (error != null) {
                        currentState.validationErrors + ("monthlyIncome" to error)
                    } else {
                        currentState.validationErrors - "monthlyIncome"
                    },
            )
    }

    private fun updateCollateralType(type: String) {
        val currentState = _cashLoanApplicationState.value
        _cashLoanApplicationState.value = currentState.copy(collateralType = type)
    }

    private fun updateCollateralValue(value: String) {
        val sanitizedValue = value.filter { it.isDigit() || it == '.' }
        val currentState = _cashLoanApplicationState.value
        _cashLoanApplicationState.value = currentState.copy(collateralValue = sanitizedValue)
    }

    private fun calculateCashLoanTerms() {
        val currentState = _cashLoanApplicationState.value
        if (!currentState.isFormValid()) return

        _cashLoanApplicationState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val request =
                CashLoanCalculationRequest(
                    loanAmount = currentState.loanAmount.toDoubleOrNull() ?: 0.0,
                    repaymentPeriod = currentState.repaymentPeriod,
                    monthlyIncome = currentState.monthlyIncome.toDoubleOrNull() ?: 0.0,
                    employerIndustry = currentState.application?.employerIndustry ?: "",
                    collateralValue = currentState.collateralValue.toDouble(),
                )

            val result =
                calculateCashLoanTermsUseCase(
                    request.loanAmount,
                    request.repaymentPeriod,
                    request.employerIndustry,
                    request.collateralValue,
                    request.monthlyIncome,
                )

            when (result) {
                is com.soshopay.domain.repository.Result.Success<CashLoanTerms> -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            calculatedTerms = result.data,
                            showTermsDialog = true,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _cashLoanApplicationState.value = currentState.copy(isLoading = true)
                }

                is com.soshopay.domain.repository.Result.Error -> TODO()
                com.soshopay.domain.repository.Result.Loading -> TODO()
                is com.soshopay.domain.repository.Result.Success<*> -> TODO()
            }
        }
    }

    private fun showTermsDialog() {
        _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(showTermsDialog = true)
    }

    private fun dismissTermsDialog() {
        _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(showTermsDialog = false)
    }

    private fun acceptTerms() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showTermsDialog = false,
                showConfirmationDialog = true,
            )
    }

    private fun showConfirmationDialog() {
        _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(showConfirmationDialog = true)
    }

    private fun dismissConfirmationDialog() {
        _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(showConfirmationDialog = false)
    }

    private fun submitCashLoanApplication() {
        val currentState = _cashLoanApplicationState.value
        val terms = currentState.calculatedTerms ?: return

        _cashLoanApplicationState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val application =
                CashLoanApplication(
                    id = "",
                    userId = "",
                    applicationId = "",
                    loanType = LoanType.CASH,
                    loanAmount = currentState.loanAmount.toDoubleOrNull() ?: 0.0,
                    loanPurpose = currentState.loanPurpose,
                    repaymentPeriod = currentState.repaymentPeriod,
                    collateralValue = currentState.collateralValue.toDoubleOrNull() ?: 0.0,
                    calculatedTerms = terms,
                    status = ApplicationStatus.SUBMITTED,
                    submittedAt = System.currentTimeMillis(),
                    acceptedTerms = true,
                    employerIndustry = currentState.application?.employerIndustry ?: "",
                    collateralDetails = currentState.application?.collateralDetails ?: "",
                )

            val result = submitCashLoanApplicationUseCase(application)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _cashLoanApplicationState.value = currentState.copy(isLoading = false)
                    // Navigate to loan history to show submitted application
                    _navigationEvents.emit(LoanPaymentNavigation.ToLoanHistory)
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                    // Save as draft for retry later
                    saveDraftApplication()
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _cashLoanApplicationState.value = currentState.copy(isLoading = true)
                }
            }
        }
    }

    private fun saveDraftApplication() {
        val currentState = _cashLoanApplicationState.value

        viewModelScope.launch {
            val application =
                CashLoanApplication(
                    id = "",
                    userId = "",
                    applicationId = "",
                    loanType = LoanType.CASH,
                    loanAmount = currentState.loanAmount.toDoubleOrNull() ?: 0.0,
                    loanPurpose = currentState.loanPurpose,
                    repaymentPeriod = currentState.repaymentPeriod,
                    collateralValue = currentState.collateralValue.toDoubleOrNull() ?: 0.0,
                    calculatedTerms = currentState.calculatedTerms,
                    status = ApplicationStatus.SUBMITTED,
                    submittedAt = System.currentTimeMillis(),
                    acceptedTerms = true,
                    employerIndustry = currentState.application?.employerIndustry ?: "",
                    collateralDetails = currentState.application?.collateralDetails ?: "",
                )

            saveCashLoanDraftUseCase(application)
        }
    }

    // ========== PAYGO APPLICATION OPERATIONS ==========

    private fun loadPayGoCategories() {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getPayGoCategoriesUseCase()

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            categories = result.data,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value = _payGoApplicationState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun selectCategory(category: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                selectedCategory = category,
                products = emptyList(),
                selectedProduct = null,
            )
        loadCategoryProducts(category)
    }

    private fun loadCategoryProducts(categoryId: String) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = getPayGoProductsUseCase(categoryId)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            products = result.data,
                            currentStep = PayGoStep.PRODUCT_SELECTION,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value = _payGoApplicationState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun selectProduct(product: PayGoProduct) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                selectedProduct = product,
                currentStep = PayGoStep.APPLICATION_DETAILS,
            )
    }

    private fun showProductDetails() {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(showProductDetails = true)
    }

    private fun dismissProductDetails() {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(showProductDetails = false)
    }

    private fun updateUsagePerDay(usage: String) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(usagePerDay = usage)
    }

    private fun updatePayGoRepaymentPeriod(period: String) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(repaymentPeriod = period)
    }

    private fun updateSalaryBand(band: String) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(salaryBand = band)
    }

    private fun updateGuarantorInfo(guarantor: Guarantor) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(guarantorInfo = guarantor)
    }

    private fun nextPayGoStep(step: PayGoStep) {
        val currentState = _payGoApplicationState.value
        if (currentState.canProceedToNextStep()) {
            _payGoApplicationState.value = currentState.copy(currentStep = step)
        }
    }

    private fun previousPayGoStep(step: PayGoStep) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(currentStep = step)
    }

    private fun calculatePayGoTerms() {
        val currentState = _payGoApplicationState.value
        val product = currentState.selectedProduct ?: return

        _payGoApplicationState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val request =
                PayGoCalculationRequest(
                    productId = product.id,
                    usagePerDay = currentState.usagePerDay,
                    repaymentPeriod = currentState.repaymentPeriod,
                    salaryBand = currentState.salaryBand,
                    monthlyIncome = 0.0,
                )

            val result =
                calculatePayGoTermsUseCase(
                    request.productId,
                    request.repaymentPeriod,
                    request.usagePerDay,
                    request.salaryBand,
                    request.monthlyIncome,
                )

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            calculatedTerms = result.data,
                            currentStep = PayGoStep.TERMS_REVIEW,
                            showTermsDialog = true,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value = currentState.copy(isLoading = true)
                }
            }
        }
    }

    private fun submitPayGoApplication() {
        val currentState = _payGoApplicationState.value
        val product = currentState.selectedProduct ?: return
        val terms = currentState.calculatedTerms ?: return
        val guarantor = currentState.guarantorInfo ?: return

        _payGoApplicationState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val application =
                PayGoLoanApplication(
                    id = "",
                    userId = "",
                    applicationId = "",
                    loanType = LoanType.PAYGO,
                    productId = product.id,
                    productDetails = product,
                    usagePerDay = currentState.usagePerDay,
                    repaymentPeriod = currentState.repaymentPeriod,
                    salaryBand = currentState.salaryBand,
                    guarantor = guarantor,
                    calculatedTerms = terms,
                    status = ApplicationStatus.SUBMITTED,
                    submittedAt = System.currentTimeMillis(),
                    acceptedTerms = true,
                )

            val result = submitPayGoApplicationUseCase(application)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value = currentState.copy(isLoading = false)
                    _navigationEvents.emit(LoanPaymentNavigation.ToLoanHistory)
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value = currentState.copy(isLoading = true)
                }
            }
        }
    }

    // ========== LOAN HISTORY OPERATIONS ==========

    private fun loadLoanHistory() {
        _loanHistoryState.value = _loanHistoryState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getLoanHistoryUseCase(com.soshopay.domain.model.LoanHistoryFilter.ALL, 0)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val loans = result.data.loans
                    _loanHistoryState.value =
                        _loanHistoryState.value.copy(
                            isLoading = false,
                            loans = loans,
                            filteredLoans = loans,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _loanHistoryState.value =
                        _loanHistoryState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _loanHistoryState.value = _loanHistoryState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun refreshLoanHistory() {
        _loanHistoryState.value = _loanHistoryState.value.copy(isRefreshing = true)
        loadLoanHistory()
    }

    private fun applyLoanFilter(filter: com.soshopay.android.ui.state.LoanHistoryFilter) {
        val currentState = _loanHistoryState.value
        val filteredLoans =
            when (filter) {
                com.soshopay.android.ui.state.LoanHistoryFilter.ALL -> currentState.loans
                com.soshopay.android.ui.state.LoanHistoryFilter.APPROVED ->
                    currentState.loans.filter {
                        it.status == LoanStatus.ACTIVE ||
                            it.status == LoanStatus.COMPLETED
                    }
                com.soshopay.android.ui.state.LoanHistoryFilter.REJECTED -> currentState.loans.filter { it.status == LoanStatus.CANCELLED }
                com.soshopay.android.ui.state.LoanHistoryFilter.CURRENT -> currentState.loans.filter { it.status == LoanStatus.ACTIVE }
                com.soshopay.android.ui.state.LoanHistoryFilter.COMPLETED -> currentState.loans.filter { it.status == LoanStatus.COMPLETED }
            }

        _loanHistoryState.value =
            currentState.copy(
                selectedFilter = filter,
                filteredLoans = filteredLoans,
            )
    }

    private fun selectLoan(loanId: String) {
        viewModelScope.launch {
            _navigationEvents.emit(LoanPaymentNavigation.ToLoanDetails(loanId))
        }
    }

    // ========== LOAN DETAILS OPERATIONS ==========

    private fun loadLoanDetails(loanId: String) {
        _loanDetailsState.value = _loanDetailsState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getLoanDetailsUseCase(loanId)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val loanDetails = result.data
                    _loanDetailsState.value =
                        _loanDetailsState.value.copy(
                            isLoading = false,
                            loan = loanDetails.loan,
                            loanDetails = loanDetails,
                            paymentSchedule = loanDetails.paymentSchedule,
                            showEarlyPaymentOption = loanDetails.loan.status == LoanStatus.ACTIVE,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _loanDetailsState.value =
                        _loanDetailsState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _loanDetailsState.value = _loanDetailsState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun showEarlyPaymentOption() {
        _loanDetailsState.value = _loanDetailsState.value.copy(showEarlyPaymentOption = true)
    }

    private fun hideEarlyPaymentOption() {
        _loanDetailsState.value = _loanDetailsState.value.copy(showEarlyPaymentOption = false)
    }

    private fun showLoanAgreement() {
        _loanDetailsState.value = _loanDetailsState.value.copy(showAgreementDialog = true)
    }

    private fun dismissLoanAgreement() {
        _loanDetailsState.value = _loanDetailsState.value.copy(showAgreementDialog = false)
    }

    private fun downloadLoanAgreement(loanId: String) {
        viewModelScope.launch {
            val result = downloadLoanAgreementUseCase(loanId)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _loanDetailsState.value =
                        _loanDetailsState.value.copy(
                            agreementPdfUrl = "downloaded", // In real app, this would be the file path
                            showAgreementDialog = true,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _loanDetailsState.value =
                        _loanDetailsState.value.copy(
                            errorMessage = getErrorMessage(result.exception),
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

    private fun validateLoanAmount(amount: String): String? =
        when {
            amount.isEmpty() -> "Loan amount is required"
            amount.toDoubleOrNull() == null -> "Invalid amount format"
            amount.toDouble() < 100 -> "Minimum loan amount is $100"
            amount.toDouble() > 50000 -> "Maximum loan amount is $50,000"
            else -> null
        }

    private fun validateLoanPurpose(purpose: String): String? =
        when {
            purpose.isEmpty() -> "Loan purpose is required"
            purpose.length < 10 -> "Please provide more details about loan purpose"
            else -> null
        }

    private fun validateRepaymentPeriod(period: String): String? =
        when {
            period.isEmpty() -> "Repayment period is required"
            else -> null
        }

    private fun validateMonthlyIncome(income: String): String? =
        when {
            income.isEmpty() -> "Monthly income is required"
            income.toDoubleOrNull() == null -> "Invalid income format"
            income.toDouble() < 300 -> "Minimum monthly income is $300"
            else -> null
        }

    private fun clearError() {
        _loanDashboardState.value = _loanDashboardState.value.copy(errorMessage = null)
        _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(errorMessage = null)
        _payGoApplicationState.value = _payGoApplicationState.value.copy(errorMessage = null)
        _loanHistoryState.value = _loanHistoryState.value.copy(errorMessage = null)
        _loanDetailsState.value = _loanDetailsState.value.copy(errorMessage = null)
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
        loadLoanDashboard()
    }
}
