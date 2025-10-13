package com.soshopay.android.ui.component.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.PaymentViewModel
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.model.PaymentSummary
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Payment Dashboard Screen following the Activity Diagram flow.
 *
 * Features:
 * - Payment summary overview
 * - Due and overdue payment alerts
 * - List of loans with payment status
 * - Quick payment actions
 * - Pull to refresh functionality
 * - Navigation to payment processing and history
 *
 * @param onNavigateToPaymentProcessing Callback for payment processing navigation
 * @param onNavigateToPaymentHistory Callback for payment history navigation
 * @param viewModel PaymentViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDashboardScreen(
    onNavigateToPaymentProcessing: (Loan) -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
    viewModel: PaymentViewModel = koinViewModel(),
) {
    val dashboardState by viewModel.paymentDashboardState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.ToPaymentProcessing -> onNavigateToPaymentProcessing(event.loan)
                is LoanPaymentNavigation.ToPaymentHistory -> onNavigateToPaymentHistory()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle pull to refresh
    if (pullToRefreshState.isAnimating) {
        LaunchedEffect(true) {
            viewModel.onEvent(LoanPaymentEvent.RefreshPaymentDashboard)
            pullToRefreshState.isAnimating
        }
    }

    SoshoPayTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(16.dp),
            ) {
                // Header
                item {
                    PaymentDashboardHeader(
                        onNavigateToHistory = { viewModel.onEvent(LoanPaymentEvent.LoadPaymentHistory) },
                    )
                }

                // Loading State
                if (dashboardState.isLoading && dashboardState.paymentSummaries.isEmpty()) {
                    item {
                        LoadingSection()
                    }
                }

                // Error State
                else if (dashboardState.hasErrors() && dashboardState.paymentSummaries.isEmpty()) {
                    item {
                        ErrorSection(
                            errorMessage = dashboardState.errorMessage!!,
                            onRetry = { viewModel.onEvent(LoanPaymentEvent.LoadPaymentDashboard) },
                        )
                    }
                }

                // Content
                else {
                    // Summary Cards
                    item {
                        PaymentSummaryCards(dashboardState)
                    }

                    // Alert Section (if overdue payments exist)
                    if (dashboardState.hasOverduePayments()) {
                        item {
                            OverduePaymentAlert(
                                overdueCount = dashboardState.overdueCount,
                                totalOverdueAmount =
                                    dashboardState.paymentSummaries
                                        .filter { it.isOverdue() }
                                        .sumOf { it.amountDue },
                            )
                        }
                    }

                    // Payment List Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Your Loans",
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = if (isDarkMode) Color.White else Color.Black,
                            )

                            IconButton(
                                onClick = { viewModel.onEvent(LoanPaymentEvent.RefreshPaymentDashboard) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = if (isDarkMode) Color.White else Color.Black,
                                )
                            }
                        }
                    }

                    // Payment List
                    if (dashboardState.paymentSummaries.isEmpty()) {
                        item {
                            EmptyPaymentList()
                        }
                    } else {
                        items(dashboardState.paymentSummaries) { paymentSummary ->
                            PaymentSummaryCard(
                                paymentSummary = paymentSummary,
                                onPayClick = {
                                    // Convert PaymentSummary to Loan for navigation
                                    val loan = createLoanFromSummary(paymentSummary)
                                    viewModel.onEvent(LoanPaymentEvent.SelectLoanForPayment(loan))
                                },
                            )
                        }
                    }
                }
            }

            // Pull to refresh indicator
            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
            )
        }
    }
}

@Composable
fun PullToRefreshContainer(
    modifier: Modifier,
    state: PullToRefreshState,
) {
}

/**
 * Dashboard header with title and history navigation
 */
@Composable
private fun PaymentDashboardHeader(onNavigateToHistory: () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Payments",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
            )
            Text(
                text = "Manage your loan payments",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
            )
        }

        IconButton(onClick = onNavigateToHistory) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Payment History",
                tint = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * Payment summary cards showing key metrics
 */
@Composable
private fun PaymentSummaryCards(dashboardState: com.soshopay.android.ui.state.PaymentDashboardState) {
    val isDarkMode = isSystemInDarkTheme()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Total Due Amount Card
        SummaryMetricCard(
            title = "Total Due",
            value = "${String.format("%.2f", dashboardState.totalDueAmount)}",
            icon = Icons.Default.AccountBalance,
            color =
                if (dashboardState.hasOverduePayments()) {
                    MaterialTheme.colorScheme.error
                } else {
                    if (isDarkMode) {
                        colorResource(id = R.color.yellow)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Current Loans Card
            SummaryMetricCard(
                title = "Current Loans",
                value = "${dashboardState.currentCount}",
                icon = Icons.Default.Payment,
                color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                modifier = Modifier.weight(1f),
            )

            // Overdue Count Card
            SummaryMetricCard(
                title = "Overdue",
                value = "${dashboardState.overdueCount}",
                icon = Icons.Default.Warning,
                color =
                    if (dashboardState.overdueCount > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                modifier = Modifier.weight(1f),
            )
        }

        // Next Payment Info
        if (dashboardState.nextPaymentDate != null) {
            NextPaymentCard(
                nextPaymentDate = dashboardState.nextPaymentDate!!,
                nextPaymentAmount = dashboardState.nextPaymentAmount,
            )
        }
    }
}

/**
 * Individual summary metric card
 */
@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                )
                Text(
                    text = value,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (isDarkMode) Color.White else Color.Black,
                )
            }
        }
    }
}

/**
 * Next payment information card
 */
@Composable
private fun NextPaymentCard(
    nextPaymentDate: Long,
    nextPaymentAmount: Double,
) {
    val isDarkMode = isSystemInDarkTheme()
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Next Payment Due",
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = dateFormatter.format(Date(nextPaymentDate)),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )
                    Text(
                        text = "${String.format("%.2f", nextPaymentAmount)}",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = colorResource(id = R.color.yellow),
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

/**
 * Overdue payment alert
 */
@Composable
private fun OverduePaymentAlert(
    overdueCount: Int,
    totalOverdueAmount: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overdue Payments",
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = "$overdueCount payment${if (overdueCount > 1) "s" else ""} overdue • ${String.format(
                        "%.2f",
                        totalOverdueAmount,
                    )}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

/**
 * Individual payment summary card
 */
@Composable
private fun PaymentSummaryCard(
    paymentSummary: PaymentSummary,
    onPayClick: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onPayClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = paymentSummary.productName ?: "Cash Loan",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )
                    Text(
                        text = "Loan ID: ${paymentSummary.loanId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    )
                }

                PaymentStatusBadge(paymentSummary.status)
            }

            // Payment Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    )
                    Text(
                        text = dateFormatter.format(Date(paymentSummary.dueDate)),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount Due",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    )
                    Text(
                        text = "${String.format("%.2f", paymentSummary.amountDue)}",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color =
                            if (paymentSummary.isOverdue()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            },
                    )
                }
            }

            // Action Button
            Button(
                onClick = onPayClick,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (paymentSummary.isOverdue()) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            },
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = if (paymentSummary.isOverdue()) "Pay Overdue" else "Make Payment",
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Payment status badge
 */
@Composable
private fun PaymentStatusBadge(status: PaymentStatus) {
    val (text, color) =
        when (status) {
            PaymentStatus.OVERDUE -> "Overdue" to MaterialTheme.colorScheme.error
            PaymentStatus.CURRENT -> "Current" to MaterialTheme.colorScheme.primary
            PaymentStatus.SUCCESSFUL -> "Paid" to Color.Green
            else -> "Pending" to Color.Gray
        }

    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
            color = color,
        )
    }
}

/**
 * Loading section
 */
@Composable
private fun LoadingSection() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
            )
            Text(
                text = "Loading payment data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * Error section
 */
@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )

            Text(
                text = "Failed to load payments",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onRetry,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Empty payment list
 */
@Composable
private fun EmptyPaymentList() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = "No Payments",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )

            Text(
                text = "No Active Loans",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "You don't have any active loans requiring payments at the moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Helper function to convert PaymentSummary to Loan for navigation
 */
private fun createLoanFromSummary(paymentSummary: PaymentSummary): Loan =
    Loan(
        id = paymentSummary.loanId,
        userId = "",
        applicationId = "",
        loanType = paymentSummary.loanType,
        originalAmount = 0.0,
        totalAmount = 0.0,
        remainingBalance = paymentSummary.amountDue,
        interestRate = 0.0,
        repaymentPeriod = "",
        disbursementDate = System.currentTimeMillis(),
        maturityDate = paymentSummary.dueDate,
        status = com.soshopay.domain.model.LoanStatus.ACTIVE,
        nextPaymentDate = paymentSummary.dueDate,
        nextPaymentAmount = paymentSummary.amountDue,
        paymentsCompleted = 0,
        totalPayments = 0,
        productName = paymentSummary.productName,
        loanPurpose = null,
        installationDate = null,
        rejectionReason = null,
        rejectionDate = null,
        createdAt = 0,
        updatedAt = 0,
    )

@Preview(showBackground = true)
@Composable
fun PaymentDashboardScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data
    }
}
