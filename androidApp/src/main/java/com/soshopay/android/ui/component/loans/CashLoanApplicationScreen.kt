package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.LoanViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Cash Loan Application Screen following the Activity Diagram flow.
 *
 * Features:
 * - Form validation with real-time feedback
 * - Loan terms calculation and display
 * - Loading states with proper UI feedback
 * - Error handling with user-friendly messages
 * - Terms acceptance dialog
 * - Final confirmation dialog
 * - Draft saving functionality
 *
 * @param onNavigateToLoanHistory Callback for loan history navigation
 * @param onNavigateBack Callback for back navigation
 * @param viewModel LoanViewModel for state management
 */
@Composable
fun CashLoanApplicationScreen(
    onNavigateToLoanHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val applicationState by viewModel.cashLoanApplicationState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

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

    val isDarkMode = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    SoshoPayTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.background else Color.White),
        ) {
            // Top Bar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.onEvent(LoanPaymentEvent.NavigateBack) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color.Black,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cash Loan Application",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (isDarkMode) Color.White else Color.Black,
                )
            }

            // Main Content
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Application Form Card
                ApplicationFormCard(
                    applicationState = applicationState,
                    onEvent = viewModel::onEvent,
                    focusManager = focusManager,
                )

                // Calculate Terms Button
                Button(
                    onClick = { viewModel.onEvent(LoanPaymentEvent.CalculateLoanTerms) },
                    enabled = applicationState.isFormValid() && !applicationState.isLoading,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(12.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                ) {
                    if (applicationState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculating Terms...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Calculate",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate Loan Terms")
                    }
                }

                // Save Draft Button
                TextButton(
                    onClick = { viewModel.onEvent(LoanPaymentEvent.SaveDraftApplication) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Save as Draft")
                }
            }

            // Terms Dialog
            if (applicationState.showTermsDialog) {
                LoanTermsDialog(
                    terms = applicationState.calculatedTerms,
                    onDismiss = { viewModel.onEvent(LoanPaymentEvent.DismissTermsDialog) },
                    onAccept = { viewModel.onEvent(LoanPaymentEvent.AcceptTerms) },
                )
            }

            // Confirmation Dialog
            if (applicationState.showConfirmationDialog) {
                ConfirmationDialog(
                    onDismiss = { viewModel.onEvent(LoanPaymentEvent.DismissConfirmationDialog) },
                    onConfirm = { viewModel.onEvent(LoanPaymentEvent.SubmitCashLoanApplication) },
                    isLoading = applicationState.isLoading,
                )
            }

            // Error Dialog
            if (applicationState.errorMessage != null) {
                ErrorDialog(
                    errorMessage = applicationState.errorMessage!!,
                    onDismiss = { viewModel.onEvent(LoanPaymentEvent.ClearError) },
                )
            }
        }
    }
}

/**
 * Application form card with all input fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationFormCard(
    applicationState: com.soshopay.android.ui.state.CashLoanApplicationState,
    onEvent: (LoanPaymentEvent) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Loan Details",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
            )

            // Loan Amount Field
            OutlinedTextField(
                value = applicationState.loanAmount,
                onValueChange = { onEvent(LoanPaymentEvent.UpdateLoanAmount(it)) },
                label = { Text("Loan Amount (USD)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Amount",
                    )
                },
                isError = applicationState.validationErrors.containsKey("loanAmount"),
                supportingText = {
                    applicationState.validationErrors["loanAmount"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            )

            // Loan Purpose Field
            OutlinedTextField(
                value = applicationState.loanPurpose,
                onValueChange = { onEvent(LoanPaymentEvent.UpdateLoanPurpose(it)) },
                label = { Text("Loan Purpose") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Purpose",
                    )
                },
                isError = applicationState.validationErrors.containsKey("loanPurpose"),
                supportingText = {
                    applicationState.validationErrors["loanPurpose"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            )

            // Repayment Period Dropdown
            var repaymentExpanded by remember { mutableStateOf(false) }
            val repaymentOptions = listOf("6 months", "12 months", "18 months", "24 months")

            ExposedDropdownMenuBox(
                expanded = repaymentExpanded,
                onExpandedChange = { repaymentExpanded = !repaymentExpanded },
            ) {
                OutlinedTextField(
                    modifier =
                        Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    readOnly = true,
                    value = applicationState.repaymentPeriod,
                    onValueChange = {},
                    label = { Text("Repayment Period") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Period",
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repaymentExpanded) },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = repaymentExpanded,
                    onDismissRequest = { repaymentExpanded = false },
                ) {
                    repaymentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onEvent(LoanPaymentEvent.UpdateRepaymentPeriod(option))
                                repaymentExpanded = false
                            },
                        )
                    }
                }
            }

            // Monthly Income Field
            OutlinedTextField(
                value = applicationState.monthlyIncome,
                onValueChange = { onEvent(LoanPaymentEvent.UpdateMonthlyIncome(it)) },
                label = { Text("Monthly Income (USD)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Income",
                    )
                },
                isError = applicationState.validationErrors.containsKey("monthlyIncome"),
                supportingText = {
                    applicationState.validationErrors["monthlyIncome"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            )

            // Collateral Type Dropdown
            var collateralExpanded by remember { mutableStateOf(false) }
            val collateralOptions = listOf("Vehicle", "Property", "Equipment", "Electronics", "Jewelry")

            ExposedDropdownMenuBox(
                expanded = collateralExpanded,
                onExpandedChange = { collateralExpanded = !collateralExpanded },
            ) {
                OutlinedTextField(
                    modifier =
                        Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    readOnly = true,
                    value = applicationState.collateralType,
                    onValueChange = {},
                    label = { Text("Collateral Type") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Collateral",
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collateralExpanded) },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = collateralExpanded,
                    onDismissRequest = { collateralExpanded = false },
                ) {
                    collateralOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onEvent(LoanPaymentEvent.UpdateCollateralType(option))
                                collateralExpanded = false
                            },
                        )
                    }
                }
            }

            // Collateral Value Field
            OutlinedTextField(
                value = applicationState.collateralValue,
                onValueChange = { onEvent(LoanPaymentEvent.UpdateCollateralValue(it)) },
                label = { Text("Collateral Value (USD)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Value",
                    )
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            )
        }
    }
}

/**
 * Loan terms dialog showing calculated terms
 */
@Composable
private fun LoanTermsDialog(
    terms: com.soshopay.domain.model.CashLoanTerms?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    if (terms == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Loan Terms Summary",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LoanTermItem("Monthly Payment", "$${String.format("%.2f", terms.monthlyPayment)}")
                LoanTermItem("Interest Rate", "${terms.interestRate}%")
                LoanTermItem("Total Amount", "$${String.format("%.2f", terms.totalAmount)}")
                LoanTermItem("Processing Fee", "$${String.format("%.2f", terms.processingFee)}")
                LoanTermItem("Total Interest", "$${String.format("%.2f", terms.getTotalInterest())}")
                LoanTermItem("Total Cost", "$${String.format("%.2f", terms.getTotalCost())}")
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text("Accept Terms")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
    )
}

/**
 * Individual loan term item
 */
@Composable
private fun LoanTermItem(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Final confirmation dialog
 */
@Composable
private fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
) {
    val isDarkMode = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Application",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )
        },
        text = {
            Text(
                text = "Are you sure you want to submit this loan application? Once submitted, you cannot make changes to the application details.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                    ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Submit Application")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text("Cancel")
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
    )
}

/**
 * Error dialog
 */
@Composable
private fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Application Error",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )
        },
        text = {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
    )
}

@Preview(showBackground = true)
@Composable
fun CashLoanApplicationScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data
    }
}
