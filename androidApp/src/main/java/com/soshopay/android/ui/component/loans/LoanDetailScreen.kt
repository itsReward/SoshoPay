package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.LoanViewModel
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Loan Details Screen
 *
 * Displays comprehensive information about a specific loan including:
 * - Loan overview (amount, status, type)
 * - Payment information (next payment, schedule)
 * - Loan progress
 * - Payment history
 * - Actions (make payment, download agreement)
 *
 * @param loanId The ID of the loan to display
 * @param onNavigateToPayment Callback for payment navigation
 * @param onNavigateBack Callback for back navigation
 * @param viewModel LoanViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsScreen(
    loanId: String,
    onNavigateToPayment: (Loan) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val detailsState by viewModel.loanDetailsState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Load loan details on first composition
    LaunchedEffect(loanId) {
        viewModel.onEvent(LoanPaymentEvent.LoadLoanDetails(loanId))
    }

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.Back -> onNavigateBack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Loan Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor =
                                if (isDarkMode) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                        ),
                )
            },
            modifier = Modifier.background(if (isDarkMode) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface),
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            if (isDarkMode) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                Color.White
                            },
                        ),
            ) {
                when {
                    detailsState.isLoading -> {
                        // Loading State
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    detailsState.errorMessage != null -> {
                        // Error State
                        ErrorContent(
                            errorMessage = detailsState.errorMessage ?: "Failed to load loan details",
                            onRetry = {
                                viewModel.onEvent(LoanPaymentEvent.LoadLoanDetails(loanId))
                            },
                            onNavigateBack = onNavigateBack,
                        )
                    }
                    detailsState.loan != null -> {
                        // Success State
                        LoanDetailsContent(
                            loan = detailsState.loan!!,
                            onNavigateToPayment = onNavigateToPayment,
                            onDownloadAgreement = {
                                viewModel.onEvent(LoanPaymentEvent.DownloadLoanAgreement(loanId))
                            },
                            isDarkMode = isDarkMode,
                        )
                    }
                    else -> {
                        // Empty State
                        EmptyContent(onNavigateBack = onNavigateBack)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDetailsContent(
    loan: Loan,
    onNavigateToPayment: (Loan) -> Unit,
    onDownloadAgreement: () -> Unit,
    isDarkMode: Boolean,
) {
    val scrollState = rememberScrollState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "US")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Loan Overview Card
        LoanOverviewCard(loan, currencyFormat, isDarkMode)

        // Payment Information Card
        if (loan.isActive()) {
            PaymentInfoCard(loan, currencyFormat, dateFormat, isDarkMode)
        }

        // Loan Progress Card
        if (loan.isActive()) {
            LoanProgressCard(loan, isDarkMode)
        }

        // Loan Details Card
        LoanInfoCard(loan, currencyFormat, dateFormat, isDarkMode)

        // Action Buttons
        ActionButtons(
            loan = loan,
            onNavigateToPayment = onNavigateToPayment,
            onDownloadAgreement = onDownloadAgreement,
            isDarkMode = isDarkMode,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LoanOverviewCard(
    loan: Loan,
    currencyFormat: NumberFormat,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Loan Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = loan.loanType.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier =
                        Modifier
                            .background(
                                color =
                                    if (loan.loanType == LoanType.CASH) {
                                        Color(0xFF4CAF50)
                                    } else {
                                        Color(0xFF2196F3)
                                    },
                                shape = RoundedCornerShape(4.dp),
                            ).padding(horizontal = 8.dp, vertical = 4.dp),
                )

                StatusBadge(loan.status)
            }

            // Product Name
            loan.productName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Loan ID
            Text(
                text = "Loan ID: ${loan.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Divider()

            // Amount Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Original Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = currencyFormat.format(loan.originalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Outstanding",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = currencyFormat.format(loan.remainingBalance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (loan.isOverdue()) Color.Red else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentInfoCard(
    loan: Loan,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Next Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            loan.nextPaymentAmount?.let { amount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Amount Due",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Text(
                            text = currencyFormat.format(amount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    loan.nextPaymentDate?.let { date ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                            Text(
                                text = dateFormat.format(Date(date)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            // Days until due
                            val daysUntilDue = loan.getDaysUntilDue()
                            Text(
                                text =
                                    when {
                                        daysUntilDue < 0 -> "${-daysUntilDue} days overdue"
                                        daysUntilDue == 0 -> "Due today"
                                        daysUntilDue == 1 -> "Due tomorrow"
                                        else -> "in $daysUntilDue days"
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    if (daysUntilDue < 0) {
                                        Color.Red
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanProgressCard(
    loan: Loan,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Repayment Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${loan.paymentsCompleted} of ${loan.totalPayments} payments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) Color.White else Color.Black,
                )
                Text(
                    text = "${loan.getProgressPercentage().toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black,
                )
            }

            LinearProgressIndicator(
                progress = (loan.getProgressPercentage() / 100).toFloat(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                color = colorResource(R.color.yellow),
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun LoanInfoCard(
    loan: Loan,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Loan Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black,
            )

            Divider()

            InfoRow("Total Amount", currencyFormat.format(loan.totalAmount))
            InfoRow("Interest Rate", "${loan.interestRate}%")
            InfoRow("Repayment Period", loan.repaymentPeriod)
            InfoRow("Disbursement Date", dateFormat.format(Date(loan.disbursementDate)))
            InfoRow("Maturity Date", dateFormat.format(Date(loan.maturityDate)))
        }
    }
}

@Composable
private fun InfoRow(
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusBadge(status: LoanStatus) {
    val (color, text) =
        when (status) {
            LoanStatus.ACTIVE -> Pair(Color(0xFF4CAF50), "Active")
            LoanStatus.COMPLETED -> Pair(Color(0xFF2196F3), "Completed")
            LoanStatus.PENDING_DISBURSEMENT -> Pair(Color(0xFFFF9800), "Pending")
            LoanStatus.DEFAULTED -> Pair(Color(0xFFF44336), "Defaulted")
            LoanStatus.CANCELLED -> Pair(Color(0xFF9E9E9E), "Cancelled")
        }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier =
            Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp),
                ).padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun ActionButtons(
    loan: Loan,
    onNavigateToPayment: (Loan) -> Unit,
    onDownloadAgreement: () -> Unit,
    isDarkMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Make Payment Button (only for active loans)
        if (loan.isActive()) {
            Button(
                onClick = { onNavigateToPayment(loan) },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.yellow),
                    ),
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Make Payment")
            }
        }

        // Download Agreement Button
        OutlinedButton(
            onClick = onDownloadAgreement,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Agreement")
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color.Red,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onNavigateBack) {
                Text("Go Back")
            }
            Button(
                onClick = onRetry,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.yellow),
                        contentColor = Color.White,
                    ),
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyContent(onNavigateBack: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No Data",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loan details not available",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}
