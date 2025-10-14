package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.loans.steps.*
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.LoanViewModel
import com.soshopay.domain.model.CashLoanApplicationStep
import org.koin.androidx.compose.koinViewModel

/**
 * Cash Loan Application Screen - Multi-Step Wizard.
 *
 * This is the main container screen that orchestrates the 5-step loan application process:
 * 1. Loan Details
 * 2. Income & Employment
 * 3. Collateral Information
 * 4. Terms Review
 * 5. Final Confirmation
 *
 * Features:
 * - Step indicator showing progress
 * - Navigation between steps
 * - Auto-save on field changes
 * - Form validation
 * - Draft loading
 *
 * Following MVVM and Clean Architecture patterns with SOLID principles.
 *
 * @param onNavigateToLoanHistory Callback to navigate to loan history
 * @param onNavigateBack Callback to navigate back
 * @param viewModel LoanViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashLoanApplicationScreen(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val applicationState by viewModel.cashLoanApplicationState.collectAsState()
    val navigationEvents = viewModel.navigationEvents
    val isDarkMode = isSystemInDarkTheme()

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.ToLoanHistory -> onNavigateToLoanHistory()
                is LoanPaymentNavigation.Back -> onNavigateBack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    // Initialize application on first composition
    LaunchedEffect(Unit) {
        viewModel.onEvent(LoanPaymentEvent.InitializeCashLoanApplication)
    }

    // Calculate completed steps
    val completedSteps = remember(applicationState) {
        buildSet {
            if (applicationState.loanAmount.isNotEmpty() &&
                applicationState.loanPurpose.isNotEmpty() &&
                applicationState.repaymentPeriod.isNotEmpty()) {
                add(1)
            }
            if (applicationState.monthlyIncome.isNotEmpty() &&
                applicationState.employerIndustry.isNotEmpty()) {
                add(2)
            }
            if (applicationState.collateralType.isNotEmpty() &&
                applicationState.collateralValue.isNotEmpty() &&
                applicationState.collateralDocuments.isNotEmpty()) {
                add(3)
            }
            if (applicationState.calculatedTerms != null) {
                add(4)
            }
        }
    }

    SoshoPayTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Cash Loan Application",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            // Auto-save indicator
                            applicationState.getAutoSaveMessage()?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (applicationState.currentStep.canNavigateBack) {
                                    viewModel.onEvent(LoanPaymentEvent.PreviousStep)
                                } else {
                                    viewModel.onEvent(LoanPaymentEvent.CancelApplication)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (applicationState.currentStep.isFirstStep()) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.ArrowBack
                                },
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkMode) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.White
                        }
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkMode) {
                            MaterialTheme.colorScheme.background
                        } else {
                            Color.White
                        }
                    )
                    .padding(paddingValues)
            ) {
                // Step Indicator
                StepIndicator(
                    currentStep = applicationState.currentStep,
                    completedSteps = completedSteps
                )

                Divider()

                // Main content based on current step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (applicationState.currentStep) {
                        CashLoanApplicationStep.LOAN_DETAILS -> {
                            CashLoanStep1Screen(
                                loanAmount = applicationState.loanAmount,
                                loanPurpose = applicationState.loanPurpose,
                                repaymentPeriod = applicationState.repaymentPeriod,
                                validationErrors = applicationState.validationErrors,
                                onEvent = viewModel::onEvent
                            )
                        }
                        CashLoanApplicationStep.INCOME_EMPLOYMENT -> {
                            CashLoanStep2Screen(
                                monthlyIncome = applicationState.monthlyIncome,
                                employerIndustry = applicationState.employerIndustry,
                                validationErrors = applicationState.validationErrors,
                                onEvent = viewModel::onEvent
                            )
                        }
                        CashLoanApplicationStep.COLLATERAL_INFO -> {
                            CashLoanStep3Screen(
                                collateralType = applicationState.collateralType,
                                collateralValue = applicationState.collateralValue,
                                collateralDetails = applicationState.collateralDetails,
                                collateralDocuments = applicationState.collateralDocuments,
                                validationErrors = applicationState.validationErrors,
                                uploadingDocument = applicationState.uploadingDocument,
                                uploadProgress = applicationState.uploadProgress,
                                onEvent = viewModel::onEvent
                            )
                        }
                        CashLoanApplicationStep.TERMS_REVIEW -> {
                            CashLoanStep4Screen(
                                calculatedTerms = applicationState.calculatedTerms,
                                isCalculating = applicationState.isCalculating,
                                canCalculate = applicationState.canCalculateTerms(),
                                onEvent = viewModel::onEvent
                            )
                        }
                        CashLoanApplicationStep.CONFIRMATION -> {
                            CashLoanStep5Screen(
                                loanAmount = applicationState.loanAmount,
                                loanPurpose = applicationState.loanPurpose,
                                repaymentPeriod = applicationState.repaymentPeriod,
                                monthlyIncome = applicationState.monthlyIncome,
                                employerIndustry = applicationState.employerIndustry,
                                collateralType = applicationState.collateralType,
                                collateralValue = applicationState.collateralValue,
                                collateralDocuments = applicationState.collateralDocuments,
                                calculatedTerms = applicationState.calculatedTerms,
                                isSubmitting = applicationState.isLoading,
                                onEvent = viewModel::onEvent
                            )
                        }
                    }

                    // Loading overlay
                    if (applicationState.isAnyOperationInProgress() &&
                        !applicationState.uploadingDocument) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Bottom navigation buttons (except for confirmation step)
                if (applicationState.currentStep != CashLoanApplicationStep.CONFIRMATION) {
                    Divider()

                    BottomNavigationButtons(
                        currentStep = applicationState.currentStep,
                        canProceed = applicationState.canProceedToNext(),
                        onPreviousClick = {
                            viewModel.onEvent(LoanPaymentEvent.PreviousStep)
                        },
                        onNextClick = {
                            viewModel.onEvent(LoanPaymentEvent.NextStep)
                        }
                    )
                }
            }

            // Error Snackbar
            applicationState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(LoanPaymentEvent.ClearValidationErrors)
                            }
                        ) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

/**
 * Bottom navigation buttons for stepping through the wizard.
 */
@Composable
private fun BottomNavigationButtons(
    currentStep: CashLoanApplicationStep,
    canProceed: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Previous button (only show if not first step)
        if (!currentStep.isFirstStep()) {
            OutlinedButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Previous")
            }
        }

        // Next button
        Button(
            onClick = onNextClick,
            enabled = canProceed,
            modifier = Modifier
                .weight(if (currentStep.isFirstStep()) 1f else 1f)
                .height(56.dp)
        ) {
            Text(
                when (currentStep) {
                    CashLoanApplicationStep.TERMS_REVIEW -> "Review & Accept"
                    else -> "Next"
                }
            )
        }
    }
}