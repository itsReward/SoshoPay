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
import com.soshopay.domain.model.Address
import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanApplicationStep
import com.soshopay.domain.model.CashLoanCalculationRequest
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.Guarantor
import com.soshopay.domain.model.LoanHistoryFilter
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType
import com.soshopay.domain.model.PayGoCalculationRequest
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoProduct
import com.soshopay.domain.model.PayGoStep
import com.soshopay.domain.model.VerificationStatus
import com.soshopay.domain.usecase.loan.CalculateCashLoanTermsUseCase
import com.soshopay.domain.usecase.loan.CalculatePayGoTermsUseCase
import com.soshopay.domain.usecase.loan.DownloadLoanAgreementUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanFormDataUseCase
import com.soshopay.domain.usecase.loan.GetCurrentLoansUseCase
import com.soshopay.domain.usecase.loan.GetLoanDetailsUseCase
import com.soshopay.domain.usecase.loan.GetLoanHistoryUseCase
import com.soshopay.domain.usecase.loan.GetPayGoCategoriesUseCase
import com.soshopay.domain.usecase.loan.GetPayGoDraftUseCase
import com.soshopay.domain.usecase.loan.GetPayGoProductsUseCase
import com.soshopay.domain.usecase.loan.SaveCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.SavePayGoDraftUseCase
import com.soshopay.domain.usecase.loan.SubmitCashLoanApplicationUseCase
import com.soshopay.domain.usecase.loan.SubmitPayGoApplicationUseCase
import com.soshopay.domain.usecase.loan.UploadCollateralDocumentUseCase
import com.soshopay.domain.usecase.profile.GetUserProfileUseCase
import com.soshopay.domain.usecase.profile.ValidateProfileCompletionUseCase
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    private val uploadCollateralDocumentUseCase: UploadCollateralDocumentUseCase,
    private val getPayGoDraftUseCase: GetPayGoDraftUseCase,
    private val savePayGoDraftUseCase: SavePayGoDraftUseCase,
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
            is LoanPaymentEvent.NextStep -> nextStep()
            is LoanPaymentEvent.PreviousStep -> previousStep()
            is LoanPaymentEvent.CancelApplication -> cancelApplication()

            // ========== CASH LOAN APPLICATION - NAVIGATION EVENTS ==========
            is LoanPaymentEvent.InitializeCashLoanApplication -> initializeCashLoanApplication()
            is LoanPaymentEvent.LoadCashLoanDraft -> loadCashLoanDraft()
            is LoanPaymentEvent.NavigateToStep -> navigateToStep(event.step)

            // ========== CASH LOAN APPLICATION - STEP 2: INCOME & EMPLOYMENT ==========
            is LoanPaymentEvent.UpdateEmployerIndustry -> updateEmployerIndustry(event.industry) // ⚠️ ADD THIS LINE

            // ========== CASH LOAN APPLICATION - STEP 3: COLLATERAL INFORMATION ==========
            is LoanPaymentEvent.UpdateCollateralDetails -> updateCollateralDetails(event.details) // ⚠️ ADD THIS LINE
            is LoanPaymentEvent.UploadCollateralDocument ->
                uploadCollateralDocument( // ⚠️ ADD THIS LINE
                    event.fileBytes,
                    event.fileName,
                    event.fileType,
                )
            is LoanPaymentEvent.RemoveCollateralDocument -> removeCollateralDocument(event.documentId) // ⚠️ ADD THIS LINE

            // ========== CASH LOAN APPLICATION - STEP 4: TERMS ==========
            is LoanPaymentEvent.ClearValidationErrors -> clearValidationErrors()

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
            is LoanPaymentEvent.NextPayGoStep -> nextPayGoStep()
            is LoanPaymentEvent.PreviousPayGoStep -> previousPayGoStep()
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

    // ========== CASH LOAN APPLICATION - MULTI-STEP WIZARD ==========

    /**
     * Initializes the cash loan application.
     * Loads form data, checks for existing draft, and pre-populates fields from user profile.
     */
    private fun initializeCashLoanApplication() {
        viewModelScope.launch {
            _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(isLoading = true)

            // Load form data
            val formDataResult = getCashLoanFormDataUseCase()

            // Get user profile for pre-population
            val userProfileResult = getUserProfileUseCase()

            // Check for existing draft
            val draftResult =
                when (userProfileResult) {
                    is Result.Success -> getCashLoanDraftUseCase(userProfileResult.data.id)
                    else -> Result.Success(null)
                }

            when {
                formDataResult is Result.Success<CashLoanFormData> && userProfileResult is Result.Success -> {
                    val formData = formDataResult.data
                    val user = userProfileResult.data
                    val draft = (draftResult as? Result.Success<CashLoanApplication?>)?.data

                    if (draft != null) {
                        // Load existing draft
                        _cashLoanApplicationState.value =
                            _cashLoanApplicationState.value.copy(
                                formData = formData as CashLoanFormData?,
                                application = draft,
                                currentStep = draft.currentStep,
                                loanAmount = draft.loanAmount.toString(),
                                loanPurpose = draft.loanPurpose,
                                repaymentPeriod = draft.repaymentPeriod,
                                monthlyIncome = draft.monthlyIncome.toString(),
                                employerIndustry = draft.employerIndustry,
                                collateralType = draft.collateralType,
                                collateralValue = draft.collateralValue.toString(),
                                collateralDetails = draft.collateralDetails,
                                collateralDocuments = draft.collateralDocuments,
                                calculatedTerms = draft.calculatedTerms,
                                isLoading = false,
                            )
                    } else {
                        // Pre-populate from user profile
                        _cashLoanApplicationState.value =
                            _cashLoanApplicationState.value.copy(
                                formData = formData,
                                application = CashLoanApplication.createDraft(user.id),
                                monthlyIncome = user.personalDetails?.monthlyIncome?.toString() ?: "",
                                employerIndustry = user.personalDetails?.occupation ?: "",
                                isLoading = false,
                            )
                    }
                }
                formDataResult is Result.Error -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(formDataResult.exception),
                        )
                }
                userProfileResult is Result.Error -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(userProfileResult.exception),
                        )
                }
            }
        }
    }

// ========== STEP NAVIGATION ==========

    /**
     * Navigates to a specific step in the wizard.
     */
    private fun navigateToStep(step: CashLoanApplicationStep) {
        val currentState = _cashLoanApplicationState.value

        // Validate current step before allowing navigation forward
        if (step.stepNumber > currentState.currentStep.stepNumber) {
            if (!currentState.isCurrentStepValid()) {
                validateCurrentStep()
                return
            }
        }

        _cashLoanApplicationState.value = currentState.copy(currentStep = step)
        autoSaveDraft()
    }

    /**
     * Moves to the next step in the wizard.
     */
    private fun nextStep() {
        val currentState = _cashLoanApplicationState.value

        // Validate current step
        if (!currentState.isCurrentStepValid()) {
            validateCurrentStep()
            return
        }

        val nextStep = currentState.currentStep.next()
        if (nextStep != null) {
            _cashLoanApplicationState.value =
                currentState.copy(
                    currentStep = nextStep,
                    errorMessage = null,
                    validationErrors = emptyMap(),
                )
            autoSaveDraft()
        }
    }

    /**
     * Moves to the previous step in the wizard.
     */
    private fun previousStep() {
        val currentState = _cashLoanApplicationState.value
        val previousStep = currentState.currentStep.previous()

        if (previousStep != null) {
            _cashLoanApplicationState.value =
                currentState.copy(
                    currentStep = previousStep,
                    errorMessage = null,
                    validationErrors = emptyMap(),
                )
        }
    }

// ========== STEP 1: LOAN DETAILS ==========

    /**
     * Updates the loan amount and triggers validation.
     */
    private fun updateLoanAmount(amount: String) {
        val sanitizedAmount = amount.filter { it.isDigit() || it == '.' }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                loanAmount = sanitizedAmount,
            )

        validateLoanAmount(sanitizedAmount)
        autoSaveDraft()
    }

    /**
     * Validates the loan amount field.
     */
    private fun validateLoanAmount(amount: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        val amountValue = amount.toDoubleOrNull()
        when {
            amount.isBlank() -> errors["loanAmount"] = "Loan amount is required"
            amountValue == null -> errors["loanAmount"] = "Invalid amount"
            amountValue < 100 -> errors["loanAmount"] = "Minimum loan amount is $100"
            amountValue > 50000 -> errors["loanAmount"] = "Maximum loan amount is $50,000"
            else -> errors.remove("loanAmount")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Updates the loan purpose.
     */
    private fun updateLoanPurpose(purpose: String) {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                loanPurpose = purpose,
            )

        validateLoanPurpose(purpose)
        autoSaveDraft()
    }

    /**
     * Validates the loan purpose field.
     */
    private fun validateLoanPurpose(purpose: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        if (purpose.isBlank()) {
            errors["loanPurpose"] = "Loan purpose is required"
        } else {
            errors.remove("loanPurpose")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Updates the repayment period.
     */
    private fun updateRepaymentPeriod(period: String) {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                repaymentPeriod = period,
            )

        validateRepaymentPeriod(period)
        autoSaveDraft()
    }

    /**
     * Validates the repayment period field.
     */
    private fun validateRepaymentPeriod(period: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        if (period.isBlank()) {
            errors["repaymentPeriod"] = "Repayment period is required"
        } else {
            errors.remove("repaymentPeriod")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

// ========== STEP 2: INCOME & EMPLOYMENT ==========

    /**
     * Updates the monthly income and triggers validation.
     */
    private fun updateMonthlyIncome(income: String) {
        val sanitizedIncome = income.filter { it.isDigit() || it == '.' }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                monthlyIncome = sanitizedIncome,
            )

        validateMonthlyIncome(sanitizedIncome)
        autoSaveDraft()
    }

    /**
     * Validates the monthly income field.
     */
    private fun validateMonthlyIncome(income: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        val incomeValue = income.toDoubleOrNull()
        when {
            income.isBlank() -> errors["monthlyIncome"] = "Monthly income is required"
            incomeValue == null -> errors["monthlyIncome"] = "Invalid income amount"
            incomeValue < 150 -> errors["monthlyIncome"] = "Minimum monthly income is $150"
            else -> errors.remove("monthlyIncome")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Updates the employer industry.
     */
    private fun updateEmployerIndustry(industry: String) {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                employerIndustry = industry,
            )

        validateEmployerIndustry(industry)
        autoSaveDraft()
    }

    /**
     * Validates the employer industry field.
     */
    private fun validateEmployerIndustry(industry: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        if (industry.isBlank()) {
            errors["employerIndustry"] = "Employer industry is required"
        } else {
            errors.remove("employerIndustry")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

// ========== STEP 3: COLLATERAL INFORMATION ==========

    /**
     * Updates the collateral type.
     */
    private fun updateCollateralType(type: String) {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                collateralType = type,
            )

        validateCollateralType(type)
        autoSaveDraft()
    }

    /**
     * Validates the collateral type field.
     */
    private fun validateCollateralType(type: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        if (type.isBlank()) {
            errors["collateralType"] = "Collateral type is required"
        } else {
            errors.remove("collateralType")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Updates the collateral value and triggers validation.
     */
    private fun updateCollateralValue(value: String) {
        val sanitizedValue = value.filter { it.isDigit() || it == '.' }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                collateralValue = sanitizedValue,
            )

        validateCollateralValue(sanitizedValue)
        autoSaveDraft()
    }

    /**
     * Validates the collateral value field.
     */
    private fun validateCollateralValue(value: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        val valueAmount = value.toDoubleOrNull()
        when {
            value.isBlank() -> errors["collateralValue"] = "Collateral value is required"
            valueAmount == null -> errors["collateralValue"] = "Invalid value"
            valueAmount <= 0 -> errors["collateralValue"] = "Collateral value must be greater than zero"
            else -> errors.remove("collateralValue")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Updates the collateral details.
     */
    private fun updateCollateralDetails(details: String) {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                collateralDetails = details,
            )

        validateCollateralDetails(details)
        autoSaveDraft()
    }

    /**
     * Validates the collateral details field.
     */
    private fun validateCollateralDetails(details: String) {
        val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()

        if (details.isBlank()) {
            errors["collateralDetails"] = "Collateral details are required"
        } else {
            errors.remove("collateralDetails")
        }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = errors,
            )
    }

    /**
     * Uploads a collateral document.
     */
    private fun uploadCollateralDocument(
        fileBytes: ByteArray,
        fileName: String,
        fileType: String,
    ) {
        viewModelScope.launch {
            _cashLoanApplicationState.value =
                _cashLoanApplicationState.value.copy(
                    uploadingDocument = true,
                    uploadProgress = 0f,
                )

            val applicationId = _cashLoanApplicationState.value.application?.id ?: "draft_${System.currentTimeMillis()}"

            // Simulate upload progress (in real implementation, use actual progress)
            _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(uploadProgress = 0.3f)

            val result =
                uploadCollateralDocumentUseCase(
                    fileBytes = fileBytes,
                    fileName = fileName,
                    fileType = fileType,
                    applicationId = applicationId,
                )

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    val currentDocs = _cashLoanApplicationState.value.collateralDocuments
                    val updatedDocs = currentDocs + result.data

                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            collateralDocuments = updatedDocs,
                            uploadingDocument = false,
                            uploadProgress = 1f,
                        )

                    // Clear validation error for documents
                    val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()
                    errors.remove("collateralDocuments")
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            validationErrors = errors,
                        )

                    autoSaveDraft()
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            uploadingDocument = false,
                            uploadProgress = 0f,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(uploadProgress = 0.6f)
                }
            }
        }
    }

    /**
     * Removes a collateral document.
     */
    private fun removeCollateralDocument(documentId: String) {
        val currentDocs = _cashLoanApplicationState.value.collateralDocuments
        val updatedDocs = currentDocs.filter { it.id != documentId }

        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                collateralDocuments = updatedDocs,
            )

        // Validate documents after removal
        if (updatedDocs.isEmpty()) {
            val errors = _cashLoanApplicationState.value.validationErrors.toMutableMap()
            errors["collateralDocuments"] = "At least one collateral document is required"
            _cashLoanApplicationState.value =
                _cashLoanApplicationState.value.copy(
                    validationErrors = errors,
                )
        }

        autoSaveDraft()
    }

// ========== VALIDATION & DRAFT MANAGEMENT ==========

    /**
     * Validates the current step and updates validation errors.
     */
    private fun validateCurrentStep() {
        val currentState = _cashLoanApplicationState.value
        val application = buildApplicationFromState(currentState)

        val validationResult = application.validateCurrentStep()

        if (!validationResult.isValid) {
            val errors = validationResult.errors.associateBy { it }
            _cashLoanApplicationState.value =
                currentState.copy(
                    validationErrors = errors,
                    errorMessage = "Please fix the errors before proceeding",
                )
        }
    }

    /**
     * Clears all validation errors.
     */
    private fun clearValidationErrors() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                validationErrors = emptyMap(),
                errorMessage = null,
            )
    }

    /**
     * Auto-saves the draft application.
     * Debounced to avoid excessive saves.
     */
    private var autoSaveJob: Job? = null

    private fun autoSaveDraft() {
        autoSaveJob?.cancel()
        autoSaveJob =
            viewModelScope.launch {
                delay(1000) // Debounce for 1 second

                _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(isSaving = true)

                val currentState = _cashLoanApplicationState.value
                val application = buildApplicationFromState(currentState)

                val result = saveCashLoanDraftUseCase(application)

                when (result) {
                    is com.soshopay.domain.repository.Result.Success -> {
                        _cashLoanApplicationState.value =
                            _cashLoanApplicationState.value.copy(
                                isSaving = false,
                                lastAutoSaveTime = System.currentTimeMillis(),
                            )
                    }
                    is com.soshopay.domain.repository.Result.Error -> {
                        _cashLoanApplicationState.value =
                            _cashLoanApplicationState.value.copy(
                                isSaving = false,
                            )
                    }
                    is com.soshopay.domain.repository.Result.Loading -> {
                        // Continue showing saving state
                    }
                }
            }
    }

    /**
     * Manually saves the draft.
     */
    private fun saveDraft() {
        viewModelScope.launch {
            _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(isSaving = true)

            val currentState = _cashLoanApplicationState.value
            val application = buildApplicationFromState(currentState)

            val result = saveCashLoanDraftUseCase(application)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            isSaving = false,
                            lastAutoSaveTime = System.currentTimeMillis(),
                            errorMessage = null,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            isSaving = false,
                            errorMessage = "Failed to save draft: ${getErrorMessage(result.exception)}",
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    // Continue showing saving state
                }
            }
        }
    }

    /**
     * Loads an existing draft application.
     */
    private fun loadCashLoanDraft() {
        viewModelScope.launch {
            _cashLoanApplicationState.value = _cashLoanApplicationState.value.copy(isLoading = true)

            val userResult = getUserProfileUseCase()

            when (userResult) {
                is Result.Success -> {
                    val userId = userResult.data.id
                    val draftResult = getCashLoanDraftUseCase(userId)

                    when (draftResult) {
                        is com.soshopay.domain.repository.Result.Success -> {
                            val draft = draftResult.data
                            if (draft != null) {
                                _cashLoanApplicationState.value =
                                    _cashLoanApplicationState.value.copy(
                                        application = draft,
                                        currentStep = draft.currentStep,
                                        loanAmount = draft.loanAmount.toString(),
                                        loanPurpose = draft.loanPurpose,
                                        repaymentPeriod = draft.repaymentPeriod,
                                        monthlyIncome = draft.monthlyIncome.toString(),
                                        employerIndustry = draft.employerIndustry,
                                        collateralType = draft.collateralType,
                                        collateralValue = draft.collateralValue.toString(),
                                        collateralDetails = draft.collateralDetails,
                                        collateralDocuments = draft.collateralDocuments,
                                        calculatedTerms = draft.calculatedTerms,
                                        isLoading = false,
                                    )
                            } else {
                                _cashLoanApplicationState.value =
                                    _cashLoanApplicationState.value.copy(
                                        isLoading = false,
                                        errorMessage = "No draft found",
                                    )
                            }
                        }
                        is com.soshopay.domain.repository.Result.Error -> {
                            _cashLoanApplicationState.value =
                                _cashLoanApplicationState.value.copy(
                                    isLoading = false,
                                    errorMessage = getErrorMessage(draftResult.exception),
                                )
                        }
                        is com.soshopay.domain.repository.Result.Loading -> {
                            // Continue showing loading state
                        }
                    }
                }
                is Result.Error -> {
                    _cashLoanApplicationState.value =
                        _cashLoanApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(userResult.exception),
                        )
                }
                is Result.Loading -> {
                    // Continue showing loading state
                }
            }
        }
    }

    /**
     * Builds a CashLoanApplication object from the current UI state.
     */
    @OptIn(ExperimentalTime::class)
    private fun buildApplicationFromState(state: CashLoanApplicationState): CashLoanApplication {
        val existingApplication = state.application ?: CashLoanApplication.createDraft("")

        return existingApplication.copy(
            currentStep = state.currentStep,
            loanAmount = state.loanAmount.toDoubleOrNull() ?: 0.0,
            loanPurpose = state.loanPurpose,
            repaymentPeriod = state.repaymentPeriod,
            monthlyIncome = state.monthlyIncome.toDoubleOrNull() ?: 0.0,
            employerIndustry = state.employerIndustry,
            collateralType = state.collateralType,
            collateralValue = state.collateralValue.toDoubleOrNull() ?: 0.0,
            collateralDetails = state.collateralDetails,
            collateralDocuments = state.collateralDocuments,
            calculatedTerms = state.calculatedTerms,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

// ========== STEP 4: TERMS CALCULATION ==========

    /**
     * Calculates the loan terms based on the application data.
     */
    private fun calculateCashLoanTerms() {
        viewModelScope.launch {
            val currentState = _cashLoanApplicationState.value

            _cashLoanApplicationState.value = currentState.copy(isCalculating = true)

            val request =
                CashLoanCalculationRequest(
                    loanAmount = currentState.loanAmount.toDoubleOrNull() ?: 0.0,
                    repaymentPeriod = currentState.repaymentPeriod,
                    employerIndustry = currentState.employerIndustry,
                    collateralValue = currentState.collateralValue.toDoubleOrNull() ?: 0.0,
                    monthlyIncome = currentState.monthlyIncome.toDoubleOrNull() ?: 0.0,
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
                is com.soshopay.domain.repository.Result.Success -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isCalculating = false,
                            calculatedTerms = result.data,
                            showTermsDialog = true,
                            errorMessage = null,
                        )
                    autoSaveDraft()
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isCalculating = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    // Continue showing calculating state
                }
            }
        }
    }

    /**
     * Shows the terms review dialog.
     */
    private fun showTermsDialog() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showTermsDialog = true,
            )
    }

    /**
     * Dismisses the terms review dialog.
     */
    private fun dismissTermsDialog() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showTermsDialog = false,
            )
    }

    /**
     * User accepts the calculated terms and moves to confirmation step.
     */
    private fun acceptTerms() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showTermsDialog = false,
                currentStep = CashLoanApplicationStep.CONFIRMATION,
            )
        autoSaveDraft()
    }

// ========== STEP 5: CONFIRMATION & SUBMISSION ==========

    /**
     * Shows the final confirmation dialog.
     */
    private fun showConfirmationDialog() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showConfirmationDialog = true,
            )
    }

    /**
     * Dismisses the final confirmation dialog.
     */
    private fun dismissConfirmationDialog() {
        _cashLoanApplicationState.value =
            _cashLoanApplicationState.value.copy(
                showConfirmationDialog = false,
            )
    }

    /**
     * Submits the cash loan application.
     */
    private fun submitCashLoanApplication() {
        viewModelScope.launch {
            val currentState = _cashLoanApplicationState.value

            _cashLoanApplicationState.value =
                currentState.copy(
                    isLoading = true,
                    showConfirmationDialog = false,
                )

            val application =
                buildApplicationFromState(currentState).copy(
                    acceptedTerms = true,
                    status = ApplicationStatus.SUBMITTED,
                )

            val result = submitCashLoanApplicationUseCase(application)

            when (result) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = null,
                        )

                    // Navigate to loan history
                    viewModelScope.launch {
                        _navigationEvents.emit(LoanPaymentNavigation.ToLoanHistory)
                    }
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _cashLoanApplicationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "Failed to submit application: ${getErrorMessage(result.exception)}",
                        )
                }
                is com.soshopay.domain.repository.Result.Loading -> {
                    // Continue showing loading state
                }
            }
        }
    }

    /**
     * Cancels the application and navigates back.
     */
    private fun cancelApplication() {
        viewModelScope.launch {
            _navigationEvents.emit(LoanPaymentNavigation.Back)
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

    private fun updateSalaryBand(band: String) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(salaryBand = band)
    }

    private fun updateGuarantorInfo(guarantor: Guarantor) {
        _payGoApplicationState.value = _payGoApplicationState.value.copy(guarantorInfo = guarantor)
    }

    /**
     * Moves to the next PayGo step in the wizard.
     */
    private fun nextPayGoStep(step: PayGoStep) {
        val currentState = _payGoApplicationState.value

        // Validate current step before proceeding
        if (!currentState.canProceedToNextStep()) {
            // Show validation error
            _payGoApplicationState.value =
                currentState.copy(
                    errorMessage = "Please complete all required fields before proceeding",
                )
            return
        }

        // Use the provided step directly
        _payGoApplicationState.value =
            currentState.copy(
                currentStep = step,
                errorMessage = null,
                validationErrors = emptyMap(),
            )
    }

    /**
     * Moves to the previous PayGo step in the wizard.
     */
    private fun previousPayGoStep(step: PayGoStep) {
        // Use the provided step directly
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                currentStep = step,
                errorMessage = null,
                validationErrors = emptyMap(),
            )
    }

    // ========== LOAN HISTORY OPERATIONS ==========

    private fun loadLoanHistory() {
        _loanHistoryState.value = _loanHistoryState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = getLoanHistoryUseCase(LoanHistoryFilter.ALL, 1)

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

    /**
     * PayGo-specific operations for LoanViewModel
     *
     * This code should be added to the existing LoanViewModel class.
     * These methods handle the complete PayGo loan application flow.
     */

    // ========== PAYGO APPLICATION OPERATIONS ==========

    /**
     * Initializes the PayGo loan application.
     * Loads categories, checks for existing draft, and sets initial state.
     */
    private fun initializePayGoApplication() {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                isLoading = true,
                errorMessage = null,
            )

        viewModelScope.launch {
            // Load PayGo categories
            when (val result = getPayGoCategoriesUseCase()) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            categories = result.data,
                            isLoading = false,
                        )

                    // Check for existing draft
                    loadPayGoDraft()
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }

                com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value = _payGoApplicationState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Loads a draft PayGo application if one exists
     */
    private fun loadPayGoDraft() {
        viewModelScope.launch {
            // Get current user ID (would come from auth state)
            val userId = "current_user_id" // TODO: Get from auth

            when (val result = getPayGoDraftUseCase(userId)) {
                is com.soshopay.domain.repository.Result.Success -> {
                    result.data?.let { draft ->
                        // Restore draft to state
                        _payGoApplicationState.value =
                            _payGoApplicationState.value.copy(
                                selectedCategory = draft.productDetails.category,
                                selectedProduct = draft.productDetails,
                                usagePerDay = draft.usagePerDay,
                                repaymentPeriod = draft.repaymentPeriod,
                                salaryBand = draft.salaryBand,
                                guarantorInfo = draft.guarantor,
                                calculatedTerms = draft.calculatedTerms,
                                acceptedTerms = draft.acceptedTerms,
                                currentStep = determinePayGoStep(draft),
                            )

                        // Load products for selected category
                        if (draft.productDetails.category.isNotEmpty()) {
                            loadPayGoProducts(draft.productDetails.category)
                        }
                    }
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    // Silently fail - no draft is not an error
                }

                com.soshopay.domain.repository.Result.Loading -> {
                }
            }
        }
    }

    /**
     * Saves the current PayGo application as a draft
     */
    private fun savePayGoDraft() {
        val state = _payGoApplicationState.value

        // Only save if we have meaningful data
        if (state.selectedProduct == null) return

        viewModelScope.launch {
            val userId = "current_user_id" // TODO: Get from auth

            val draftApplication =
                PayGoLoanApplication(
                    id = "",
                    userId = userId,
                    applicationId = "",
                    loanType = LoanType.PAYGO,
                    productId = state.selectedProduct!!.id,
                    productDetails = state.selectedProduct!!,
                    usagePerDay = state.usagePerDay,
                    repaymentPeriod = state.repaymentPeriod,
                    salaryBand = state.salaryBand,
                    guarantor =
                        state.guarantorInfo ?: Guarantor(
                            id = "",
                            applicationId = "",
                            name = "",
                            mobileNumber = "",
                            nationalId = "",
                            occupationClass = "",
                            monthlyIncome = 0.0,
                            relationshipToClient = "",
                            address =
                                Address(
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    0L,
                                ),
                            verificationStatus = VerificationStatus.PENDING,
                            createdAt = System.currentTimeMillis(),
                        ),
                    calculatedTerms = state.calculatedTerms,
                    status = ApplicationStatus.DRAFT,
                    submittedAt = System.currentTimeMillis(),
                    reviewStartedAt = null,
                    reviewCompletedAt = null,
                    acceptedTerms = state.acceptedTerms,
                )

            savePayGoDraftUseCase(draftApplication)
        }
    }

    /**
     * Updates the selected PayGo category and loads products
     */
    private fun updatePayGoCategory(category: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                selectedCategory = category,
                selectedProduct = null, // Reset product when category changes
                products = emptyList(),
            )

        loadPayGoProducts(category)
    }

    /**
     * Loads products for the selected category
     */
    private fun loadPayGoProducts(category: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                isLoading = true,
                errorMessage = null,
            )

        viewModelScope.launch {
            when (val result = getPayGoProductsUseCase(category)) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            products = result.data,
                            isLoading = false,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }

                com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = true,
                            errorMessage = null,
                        )
                }
            }
        }
    }

    /**
     * Updates the selected product
     */
    private fun updatePayGoProduct(product: PayGoProduct) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                selectedProduct = product,
            )
    }

    /**
     * Updates usage per day
     */
    private fun updatePayGoUsage(usage: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                usagePerDay = usage,
                validationErrors = _payGoApplicationState.value.validationErrors - "usagePerDay",
            )
    }

    /**
     * Updates repayment period
     */
    private fun updatePayGoRepaymentPeriod(period: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                repaymentPeriod = period,
                validationErrors = _payGoApplicationState.value.validationErrors - "repaymentPeriod",
            )
    }

    /**
     * Updates salary band
     */
    private fun updatePayGoSalaryBand(band: String) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                salaryBand = band,
                validationErrors = _payGoApplicationState.value.validationErrors - "salaryBand",
            )
    }

    /**
     * Updates guarantor information
     */
    private fun updatePayGoGuarantor(guarantor: Guarantor) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                guarantorInfo = guarantor,
                validationErrors =
                    _payGoApplicationState.value.validationErrors.filterKeys {
                        !it.startsWith("guarantor")
                    },
            )
    }

    /**
     * Updates terms acceptance status
     */
    private fun updatePayGoTermsAcceptance(accepted: Boolean) {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                acceptedTerms = accepted,
            )
    }

    /**
     * Moves to the next step in the PayGo application
     */
    private fun nextPayGoStep() {
        val currentState = _payGoApplicationState.value

        // Validate current step before proceeding
        val validationErrors = validateCurrentPayGoStep(currentState)

        if (validationErrors.isNotEmpty()) {
            _payGoApplicationState.value =
                currentState.copy(
                    validationErrors = validationErrors,
                )
            return
        }

        val nextStep =
            when (currentState.currentStep) {
                PayGoStep.CATEGORY_SELECTION -> PayGoStep.PRODUCT_SELECTION
                PayGoStep.PRODUCT_SELECTION -> PayGoStep.APPLICATION_DETAILS
                PayGoStep.APPLICATION_DETAILS -> PayGoStep.GUARANTOR_INFO
                PayGoStep.GUARANTOR_INFO -> PayGoStep.TERMS_REVIEW
                PayGoStep.TERMS_REVIEW -> PayGoStep.TERMS_REVIEW // Stay on last step
            }

        _payGoApplicationState.value =
            currentState.copy(
                currentStep = nextStep,
                validationErrors = emptyMap(),
            )

        // Auto-save when moving forward
        savePayGoDraft()
    }

    /**
     * Moves to the previous step in the PayGo application
     */
    private fun previousPayGoStep() {
        val currentState = _payGoApplicationState.value

        val previousStep =
            when (currentState.currentStep) {
                PayGoStep.CATEGORY_SELECTION -> PayGoStep.CATEGORY_SELECTION // Stay on first step
                PayGoStep.PRODUCT_SELECTION -> PayGoStep.CATEGORY_SELECTION
                PayGoStep.APPLICATION_DETAILS -> PayGoStep.PRODUCT_SELECTION
                PayGoStep.GUARANTOR_INFO -> PayGoStep.APPLICATION_DETAILS
                PayGoStep.TERMS_REVIEW -> PayGoStep.GUARANTOR_INFO
            }

        _payGoApplicationState.value =
            currentState.copy(
                currentStep = previousStep,
                validationErrors = emptyMap(),
            )
    }

    /**
     * Calculates loan terms for the PayGo application
     */
    private fun calculatePayGoTerms() {
        val state = _payGoApplicationState.value

        // Validate before calculation
        val validationErrors = validatePayGoForCalculation(state)
        if (validationErrors.isNotEmpty()) {
            _payGoApplicationState.value =
                state.copy(
                    validationErrors = validationErrors,
                )
            return
        }

        _payGoApplicationState.value =
            state.copy(
                isLoading = true,
                errorMessage = null,
            )

        viewModelScope.launch {
            val product = state.selectedProduct!!
            val monthlyIncome = extractMonthlyIncomeFromBand(state.salaryBand)

            when (
                val result =
                    calculatePayGoTermsUseCase(
                        productId = product.id,
                        repaymentPeriod = state.repaymentPeriod,
                        usagePerDay = state.usagePerDay,
                        salaryBand = state.salaryBand,
                        monthlyIncome = monthlyIncome,
                    )
            ) {
                is com.soshopay.domain.repository.Result.Success -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            calculatedTerms = result.data,
                            isLoading = false,
                        )
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }

                com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = true,
                            errorMessage = null,
                        )
                }
            }
        }
    }

    /**
     * Shows the confirmation dialog before submission
     */
    private fun showPayGoConfirmationDialog() {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                showConfirmationDialog = true,
            )
    }

    /**
     * Dismisses the confirmation dialog
     */
    private fun dismissPayGoConfirmationDialog() {
        _payGoApplicationState.value =
            _payGoApplicationState.value.copy(
                showConfirmationDialog = false,
            )
    }

    /**
     * Initiates PayGo application submission
     */
    private fun submitPayGoApplication() {
        val state = _payGoApplicationState.value

        // Final validation
        val validationErrors = validateCompletePayGoApplication(state)
        if (validationErrors.isNotEmpty()) {
            _payGoApplicationState.value =
                state.copy(
                    validationErrors = validationErrors,
                    errorMessage = "Please complete all required fields",
                )
            return
        }

        // Show confirmation dialog
        showPayGoConfirmationDialog()
    }

    /**
     * Confirms and submits the PayGo application
     */
    private fun confirmPayGoSubmission() {
        val state = _payGoApplicationState.value

        _payGoApplicationState.value =
            state.copy(
                isLoading = true,
                errorMessage = null,
                showConfirmationDialog = false,
            )

        viewModelScope.launch {
            val userId = "current_user_id" // TODO: Get from auth

            val application =
                PayGoLoanApplication(
                    id = "",
                    userId = userId,
                    applicationId = "",
                    loanType = LoanType.PAYGO,
                    productId = state.selectedProduct!!.id,
                    productDetails = state.selectedProduct!!,
                    usagePerDay = state.usagePerDay,
                    repaymentPeriod = state.repaymentPeriod,
                    salaryBand = state.salaryBand,
                    guarantor = state.guarantorInfo!!,
                    calculatedTerms = state.calculatedTerms,
                    status = ApplicationStatus.SUBMITTED,
                    submittedAt = System.currentTimeMillis(),
                    reviewStartedAt = null,
                    reviewCompletedAt = null,
                    acceptedTerms = state.acceptedTerms,
                )

            when (val result = submitPayGoApplicationUseCase(application)) {
                is com.soshopay.domain.repository.Result.Success -> {
                    // Clear draft after successful submission
                    deleteDraftPayGoApplication(userId)

                    // Navigate to loan history
                    _navigationEvents.emit(LoanPaymentNavigation.ToLoanHistory)
                }
                is com.soshopay.domain.repository.Result.Error -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }

                com.soshopay.domain.repository.Result.Loading -> {
                    _payGoApplicationState.value =
                        _payGoApplicationState.value.copy(
                            isLoading = true,
                            errorMessage = null,
                        )
                }
            }
        }
    }

// ========== VALIDATION HELPERS ==========

    /**
     * Validates the current step of PayGo application
     */
    private fun validateCurrentPayGoStep(state: PayGoApplicationState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        when (state.currentStep) {
            PayGoStep.CATEGORY_SELECTION -> {
                if (state.selectedCategory.isEmpty()) {
                    errors["category"] = "Please select a category"
                }
            }
            PayGoStep.PRODUCT_SELECTION -> {
                if (state.selectedProduct == null) {
                    errors["product"] = "Please select a product"
                }
            }
            PayGoStep.APPLICATION_DETAILS -> {
                if (state.usagePerDay.isEmpty()) {
                    errors["usagePerDay"] = "Usage per day is required"
                }
                if (state.repaymentPeriod.isEmpty()) {
                    errors["repaymentPeriod"] = "Repayment period is required"
                }
                if (state.salaryBand.isEmpty()) {
                    errors["salaryBand"] = "Salary band is required"
                }
            }
            PayGoStep.GUARANTOR_INFO -> {
                val guarantor = state.guarantorInfo
                if (guarantor == null || !guarantor.isComplete()) {
                    if (guarantor?.name.isNullOrEmpty()) {
                        errors["guarantorName"] = "Guarantor name is required"
                    }
                    if (guarantor?.mobileNumber.isNullOrEmpty()) {
                        errors["guarantorMobile"] = "Guarantor mobile number is required"
                    }
                    if (guarantor?.nationalId.isNullOrEmpty()) {
                        errors["guarantorNationalId"] = "Guarantor national ID is required"
                    }
                    if (guarantor?.occupationClass.isNullOrEmpty()) {
                        errors["guarantorOccupation"] = "Guarantor occupation is required"
                    }
                    if (guarantor?.relationshipToClient.isNullOrEmpty()) {
                        errors["guarantorRelationship"] = "Relationship is required"
                    }
                }
            }
            PayGoStep.TERMS_REVIEW -> {
                if (state.calculatedTerms == null) {
                    errors["terms"] = "Please calculate terms before proceeding"
                }
                if (!state.acceptedTerms) {
                    errors["acceptedTerms"] = "You must accept the terms to proceed"
                }
            }
        }

        return errors
    }

    /**
     * Validates PayGo application data before calculating terms
     */
    private fun validatePayGoForCalculation(state: PayGoApplicationState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (state.selectedProduct == null) {
            errors["product"] = "Product selection is required"
        }
        if (state.usagePerDay.isEmpty()) {
            errors["usagePerDay"] = "Usage per day is required"
        }
        if (state.repaymentPeriod.isEmpty()) {
            errors["repaymentPeriod"] = "Repayment period is required"
        }
        if (state.salaryBand.isEmpty()) {
            errors["salaryBand"] = "Salary band is required"
        }

        return errors
    }

    /**
     * Validates complete PayGo application before submission
     */
    private fun validateCompletePayGoApplication(state: PayGoApplicationState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate all steps
        errors.putAll(validateCurrentPayGoStep(state.copy(currentStep = PayGoStep.CATEGORY_SELECTION)))
        errors.putAll(validateCurrentPayGoStep(state.copy(currentStep = PayGoStep.PRODUCT_SELECTION)))
        errors.putAll(validateCurrentPayGoStep(state.copy(currentStep = PayGoStep.APPLICATION_DETAILS)))
        errors.putAll(validateCurrentPayGoStep(state.copy(currentStep = PayGoStep.GUARANTOR_INFO)))
        errors.putAll(validateCurrentPayGoStep(state.copy(currentStep = PayGoStep.TERMS_REVIEW)))

        return errors
    }

    /**
     * Determines the appropriate PayGo step based on draft application
     */
    private fun determinePayGoStep(draft: PayGoLoanApplication): PayGoStep =
        when {
            draft.calculatedTerms != null -> PayGoStep.TERMS_REVIEW
            draft.guarantor.isComplete() -> PayGoStep.TERMS_REVIEW
            draft.usagePerDay.isNotEmpty() &&
                draft.repaymentPeriod.isNotEmpty() &&
                draft.salaryBand.isNotEmpty() -> PayGoStep.GUARANTOR_INFO
            draft.productDetails.id.isNotEmpty() -> PayGoStep.APPLICATION_DETAILS
            draft.productDetails.category.isNotEmpty() -> PayGoStep.PRODUCT_SELECTION
            else -> PayGoStep.CATEGORY_SELECTION
        }

    /**
     * Extracts estimated monthly income from salary band string
     */
    private fun extractMonthlyIncomeFromBand(salaryBand: String): Double =
        when {
            salaryBand.contains("Below") -> 250.0
            salaryBand.contains("300") && salaryBand.contains("500") -> 400.0
            salaryBand.contains("500") && salaryBand.contains("1,000") -> 750.0
            salaryBand.contains("1,000") && salaryBand.contains("2,000") -> 1500.0
            salaryBand.contains("Above") && salaryBand.contains("2,000") -> 2500.0
            else -> 500.0 // Default
        }

    /**
     * Deletes draft PayGo application
     */
    private suspend fun deleteDraftPayGoApplication(userId: String) {
        // This would call the delete draft use case
        // For now, just a placeholder
    }

    init {
        loadLoanDashboard()
    }
}
