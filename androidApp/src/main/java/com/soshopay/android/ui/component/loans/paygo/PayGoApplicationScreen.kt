package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.loans.paygo.*
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.LoanViewModel
import com.soshopay.domain.model.PayGoStep
import org.koin.androidx.compose.koinViewModel

/**
 * PayGo Loan Application Screen
 *
 * This is the main container screen that orchestrates the 5-step PayGo loan application process:
 * 1. Category Selection
 * 2. Product Selection
 * 3. Application Details (Usage, Period, Salary)
 * 4. Guarantor Information
 * 5. Terms Review & Acceptance
 *
 * Features:
 * - Step indicator showing progress
 * - Navigation between steps
 * - Auto-save on field changes
 * - Form validation
 * - Draft loading and restoration
 * - Document upload support
 *
 * Following MVVM and Clean Architecture patterns with SOLID principles.
 *
 * @param onNavigateToLoanHistory Callback to navigate to loan history
 * @param onNavigateBack Callback to navigate back
 * @param viewModel LoanViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayGoApplicationScreen(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val applicationState by viewModel.payGoApplicationState.collectAsState()
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
        viewModel.onEvent(LoanPaymentEvent.InitializePayGoApplication)
    }

    // Auto-save draft periodically when there are changes
    LaunchedEffect(
        applicationState.selectedCategory,
        applicationState.selectedProduct,
        applicationState.usagePerDay,
        applicationState.repaymentPeriod,
        applicationState.salaryBand,
        applicationState.guarantorInfo,
    ) {
        // Auto-save after 2 seconds of inactivity
        kotlinx.coroutines.delay(2000)
        if (applicationState.selectedProduct != null) {
            viewModel.onEvent(LoanPaymentEvent.SavePayGoDraft)
        }
    }

    // Calculate completed steps
    val completedSteps =
        remember(applicationState) {
            buildSet {
                if (applicationState.selectedCategory.isNotEmpty()) {
                    add(1)
                }
                if (applicationState.selectedProduct != null) {
                    add(2)
                }
                if (applicationState.usagePerDay.isNotEmpty() &&
                    applicationState.repaymentPeriod.isNotEmpty() &&
                    applicationState.salaryBand.isNotEmpty()
                ) {
                    add(3)
                }
                if (applicationState.guarantorInfo?.isComplete() == true) {
                    add(4)
                }
                if (applicationState.calculatedTerms != null) {
                    add(5)
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
                                text = "PayGo Loan Application",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = getStepTitle(applicationState.currentStep),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        // Save draft button
                        IconButton(
                            onClick = {
                                viewModel.onEvent(LoanPaymentEvent.SavePayGoDraft)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Draft",
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                )
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.secondary),
            ) {
                // Step Indicator
                StepIndicator(
                    currentStep = applicationState.currentStep.ordinal + 1,
                    totalSteps = 5,
                    completedSteps = completedSteps,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )

                // Main content based on current step
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    when (applicationState.currentStep) {
                        PayGoStep.CATEGORY_SELECTION -> {
                            PayGoCategorySelectionStep(
                                categories = applicationState.categories,
                                selectedCategory = applicationState.selectedCategory,
                                onCategorySelected = { category ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoCategory(category),
                                    )
                                },
                                isLoading = applicationState.isLoading,
                                errorMessage = applicationState.errorMessage,
                            )
                        }

                        PayGoStep.PRODUCT_SELECTION -> {
                            PayGoProductSelectionStep(
                                products = applicationState.products,
                                selectedProduct = applicationState.selectedProduct,
                                onProductSelected = { product ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoProduct(product),
                                    )
                                },
                                isLoading = applicationState.isLoading,
                                errorMessage = applicationState.errorMessage,
                            )
                        }

                        PayGoStep.APPLICATION_DETAILS -> {
                            PayGoApplicationDetailsStep(
                                usagePerDay = applicationState.usagePerDay,
                                repaymentPeriod = applicationState.repaymentPeriod,
                                salaryBand = applicationState.salaryBand,
                                formData =
                                    viewModel.loanDashboardState
                                        .collectAsState()
                                        .value.cashLoanFormData,
                                onUsagePerDayChange = { usage ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoUsage(usage),
                                    )
                                },
                                onRepaymentPeriodChange = { period ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoRepaymentPeriod(period),
                                    )
                                },
                                onSalaryBandChange = { band ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoSalaryBand(band),
                                    )
                                },
                                validationErrors = applicationState.validationErrors,
                            )
                        }

                        PayGoStep.GUARANTOR_INFO -> {
                            PayGoGuarantorStep(
                                guarantor = applicationState.guarantorInfo,
                                formData =
                                    viewModel.loanDashboardState
                                        .collectAsState()
                                        .value.cashLoanFormData,
                                onGuarantorChange = { guarantor ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoGuarantor(guarantor),
                                    )
                                },
                                validationErrors = applicationState.validationErrors,
                            )
                        }

                        PayGoStep.TERMS_REVIEW -> {
                            PayGoTermsReviewStep(
                                calculatedTerms = applicationState.calculatedTerms,
                                selectedProduct = applicationState.selectedProduct,
                                acceptedTerms = applicationState.acceptedTerms,
                                onAcceptTermsChange = { accepted ->
                                    viewModel.onEvent(
                                        LoanPaymentEvent.UpdatePayGoTermsAcceptance(accepted),
                                    )
                                },
                                onCalculateTerms = {
                                    viewModel.onEvent(LoanPaymentEvent.CalculatePayGoTerms)
                                },
                                isCalculating = applicationState.isLoading,
                            )
                        }
                    }
                }

                // Navigation buttons
                NavigationButtons(
                    currentStep = applicationState.currentStep,
                    canProceed = applicationState.canProceedToNextStep(),
                    isLoading = applicationState.isLoading,
                    onPrevious = {
                        viewModel.onEvent(LoanPaymentEvent.PreviousPayGoStep)
                    },
                    onNext = {
                        viewModel.onEvent(LoanPaymentEvent.NextPayGoStep)
                    },
                    onSubmit = {
                        viewModel.onEvent(LoanPaymentEvent.SubmitPayGoApplication)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )
            }
        }
    }

    // Show submission confirmation dialog
    if (applicationState.showConfirmationDialog) {
        SubmissionConfirmationDialog(
            onConfirm = {
                viewModel.onEvent(LoanPaymentEvent.ConfirmPayGoSubmission)
            },
            onDismiss = {
                viewModel.onEvent(LoanPaymentEvent.DismissPayGoConfirmationDialog)
            },
        )
    }
}

/**
 * Step indicator component showing progress through the application
 */
@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    completedSteps: Set<Int>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (step in 1..totalSteps) {
            // Step circle
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color =
                        when {
                            step < currentStep || completedSteps.contains(step) ->
                                MaterialTheme.colorScheme.primary
                            step == currentStep ->
                                MaterialTheme.colorScheme.primaryContainer
                            else ->
                                MaterialTheme.colorScheme.surfaceVariant
                        },
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        if (completedSteps.contains(step) && step < currentStep) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text(
                                text = step.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal,
                                color =
                                    when {
                                        step < currentStep || completedSteps.contains(step) ->
                                            MaterialTheme.colorScheme.onPrimary
                                        step == currentStep ->
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else ->
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }
            }

            // Connector line (except after last step)
            if (step < totalSteps) {
                HorizontalDivider(
                    modifier = Modifier.weight(0.5f),
                    thickness = 2.dp,
                    color =
                        if (step < currentStep || completedSteps.contains(step)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                )
            }
        }
    }
}

/**
 * Navigation buttons for moving between steps
 */
@Composable
private fun NavigationButtons(
    currentStep: PayGoStep,
    canProceed: Boolean,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Previous button (hidden on first step)
        if (currentStep != PayGoStep.CATEGORY_SELECTION) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }
        }

        // Next/Submit button
        Button(
            onClick = {
                if (currentStep == PayGoStep.TERMS_REVIEW) {
                    onSubmit()
                } else {
                    onNext()
                }
            },
            enabled = canProceed && !isLoading,
            modifier = Modifier.weight(if (currentStep == PayGoStep.CATEGORY_SELECTION) 1f else 1f),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text =
                    when (currentStep) {
                        PayGoStep.TERMS_REVIEW -> "Submit Application"
                        else -> "Next"
                    },
            )

            if (currentStep != PayGoStep.TERMS_REVIEW && !isLoading) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                )
            }
        }
    }
}

/**
 * Confirmation dialog before final submission
 */
@Composable
private fun SubmissionConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Confirm",
            )
        },
        title = {
            Text("Confirm Submission")
        },
        text = {
            Text(
                "Are you sure you want to submit this PayGo loan application? " +
                    "Please ensure all information is correct before proceeding.",
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Review Again")
            }
        },
    )
}

/**
 * Helper function to get step title
 */
private fun getStepTitle(step: PayGoStep): String =
    when (step) {
        PayGoStep.CATEGORY_SELECTION -> "Step 1: Select Category"
        PayGoStep.PRODUCT_SELECTION -> "Step 2: Choose Product"
        PayGoStep.APPLICATION_DETAILS -> "Step 3: Application Details"
        PayGoStep.GUARANTOR_INFO -> "Step 4: Guarantor Information"
        PayGoStep.TERMS_REVIEW -> "Step 5: Review Terms"
    }
