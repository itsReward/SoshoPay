package com.soshopay.android.ui.component.loans

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.component.payments.PullToRefreshContainer
import com.soshopay.android.ui.state.LoanHistoryFilter
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.LoanViewModel
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Loan History Screen
 *
 * Displays a comprehensive list of all user loans with filtering capabilities.
 * Shows both active and inactive loans with detailed status information.
 *
 * Features:
 * - Filter by loan status (All, Approved, Rejected, Current, Completed)
 * - Pull-to-refresh functionality
 * - Detailed loan cards with visual status indicators
 * - Progress tracking for active loans
 * - Navigation to loan details
 * - Error handling with retry functionality
 * - Empty state messaging
 *
 * Following SOLID Principles:
 * - Single Responsibility: Manages only loan history display
 * - Open/Closed: Can be extended with new filters without modification
 * - Dependency Inversion: Depends on ViewModel abstraction
 *
 * @param onNavigateToLoanDetails Callback for navigation to loan details
 * @param onNavigateBack Callback for back navigation
 * @param viewModel LoanViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanHistoryScreen(
    onNavigateToLoanDetails: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val historyState by viewModel.loanHistoryState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.ToLoanDetails -> onNavigateToLoanDetails(event.loanId)
                is LoanPaymentNavigation.Back -> onNavigateBack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    // Load loan history on first composition
    LaunchedEffect(Unit) {
        viewModel.onEvent(LoanPaymentEvent.LoadLoanHistory)
    }

    val isDarkMode = isSystemInDarkTheme()
    val pullToRefreshState = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

// Handle pull to refresh
    if (pullToRefreshState.isAnimating) {
        LaunchedEffect(true) {
            viewModel.onEvent(LoanPaymentEvent.RefreshLoanHistory)
            pullToRefreshState.animateToHidden()
        }
    }

    SoshoPayTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Loan History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White,
                            titleContentColor = if (isDarkMode) Color.White else Color.Black,
                            navigationIconContentColor = if (isDarkMode) Color.White else Color.Black,
                        ),
                    scrollBehavior = scrollBehavior,
                )
            },
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White,
            contentColor = if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White,
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Filter Section
                    LoanHistoryFilterSection(
                        selectedFilter = historyState.selectedFilter,
                        onFilterSelected = { filter ->
                            viewModel.onEvent(LoanPaymentEvent.ApplyLoanFilter(filter))
                        },
                        isDarkMode = isDarkMode,
                    )

                    // Content Section
                    when {
                        historyState.isLoading && historyState.loans.isEmpty() -> {
                            LoadingSection()
                        }

                        historyState.hasErrors() && historyState.loans.isEmpty() -> {
                            ErrorSection(
                                errorMessage = historyState.errorMessage ?: "Failed to load loan history",
                                onRetry = { viewModel.onEvent(LoanPaymentEvent.RefreshLoanHistory) },
                            )
                        }

                        historyState.filteredLoans.isEmpty() -> {
                            EmptyStateSection(
                                filter = historyState.selectedFilter,
                                isDarkMode = isDarkMode,
                            )
                        }

                        else -> {
                            LoanHistoryList(
                                loans = historyState.filteredLoans,
                                onLoanClick = { loanId ->
                                    viewModel.onEvent(LoanPaymentEvent.SelectLoan(loanId))
                                },
                                isDarkMode = isDarkMode,
                            )
                        }
                    }
                }

                // Pull to refresh indicator
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

/**
 * Filter chips section for loan status filtering
 */
@Composable
private fun LoanHistoryFilterSection(
    selectedFilter: LoanHistoryFilter,
    onFilterSelected: (LoanHistoryFilter) -> Unit,
    isDarkMode: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                // .background(if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White)
                .background(Color.Transparent)
                .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleSmall,
                color = if (isDarkMode) Color.White else Color.Black,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            LoanHistoryFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorResource(id = R.color.yellow),
                            selectedLabelColor = Color.White,
                            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF5F5F5),
                            labelColor = if (isDarkMode) Color.White else Color.Black,
                        ),
                )
            }
        }
    }
}

/**
 * List of loan history items
 */
@Composable
private fun LoanHistoryList(
    loans: List<Loan>,
    onLoanClick: (String) -> Unit,
    isDarkMode: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "${loans.size} Loan${if (loans.size != 1) "s" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(loans) { loan ->
            LoanHistoryCard(
                loan = loan,
                onClick = { onLoanClick(loan.id) },
                isDarkMode = isDarkMode,
            )
        }
    }
}

/**
 * Individual loan card component
 */
@Composable
private fun LoanHistoryCard(
    loan: Loan,
    onClick: () -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header Row: Loan Type and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Loan Type and Product Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.loanType.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )
                    val productName = loan.productName
                    if (!productName.isNullOrEmpty()) {
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Status Badge
                LoanStatusBadge(
                    status = loan.status,
                    isDarkMode = isDarkMode,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LoanInfoItem(
                    label = "Original Amount",
                    value = "$${String.format("%.2f", loan.originalAmount)}",
                    isDarkMode = isDarkMode,
                )
                LoanInfoItem(
                    label = "Remaining",
                    value = "$${String.format("%.2f", loan.remainingBalance)}",
                    isDarkMode = isDarkMode,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar for Active Loans
            if (loan.status == LoanStatus.ACTIVE) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        )
                        Text(
                            text = "${loan.paymentsCompleted}/${loan.totalPayments} payments",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE0E0E0)),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(loan.getProgressPercentage().toFloat() / 100f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(colorResource(id = R.color.yellow)),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Date Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LoanInfoItem(
                    label = "Disbursement Date",
                    value = formatDate(loan.disbursementDate),
                    isDarkMode = isDarkMode,
                )
                LoanInfoItem(
                    label = "Maturity Date",
                    value = formatDate(loan.maturityDate),
                    isDarkMode = isDarkMode,
                )
            }

            // Next Payment for Active Loans
            if (loan.status == LoanStatus.ACTIVE && loan.nextPaymentDate != null) {
                Spacer(modifier = Modifier.height(12.dp))

                val daysUntilDue = loan.getDaysUntilDue()
                val isOverdue = loan.isOverdue()

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (isOverdue) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                } else {
                                    colorResource(id = R.color.yellow).copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(8.dp),
                            ).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Next Payment",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        )
                        Text(
                            text = "$${String.format("%.2f", loan.nextPaymentAmount ?: 0.0)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color =
                                if (isOverdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    if (isDarkMode) Color.White else Color.Black
                                },
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text =
                                if (isOverdue) {
                                    "OVERDUE"
                                } else {
                                    "Due in $daysUntilDue day${if (daysUntilDue != 1) "s" else ""}"
                                },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color =
                                if (isOverdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    colorResource(id = R.color.yellow)
                                },
                        )
                        val nextPaymentDate = loan.nextPaymentDate
                        Text(
                            text = if (nextPaymentDate != null)formatDate(nextPaymentDate) else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status badge component with icon and color coding
 */
@Composable
private fun LoanStatusBadge(
    status: LoanStatus,
    isDarkMode: Boolean,
) {
    val (icon, color) =
        when (status) {
            LoanStatus.ACTIVE ->
                Icons.Default.CheckCircle to colorResource(id = R.color.yellow)
            LoanStatus.COMPLETED ->
                Icons.Default.CheckCircle to Color(0xFF4CAF50)
            LoanStatus.PENDING_DISBURSEMENT ->
                Icons.Default.HourglassEmpty to Color(0xFFFF9800)
            LoanStatus.DEFAULTED, LoanStatus.CANCELLED ->
                Icons.Default.Error to MaterialTheme.colorScheme.error
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .background(
                    color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                ).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.getDisplayName(),
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = status.getDisplayName(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

/**
 * Reusable info item for displaying label-value pairs
 */
@Composable
private fun LoanInfoItem(
    label: String,
    value: String,
    isDarkMode: Boolean,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isDarkMode) Color.White else Color.Black,
        )
    }
}

/**
 * Loading state component
 */
@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.yellow),
            )
            Text(
                text = "Loading your loan history...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
        }
    }
}

/**
 * Error state component with retry button
 */
@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            androidx.compose.material3.Button(
                onClick = onRetry,
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.yellow),
                    ),
            ) {
                Text(
                    text = "Retry",
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Empty state component with contextual messaging
 */
@Composable
private fun EmptyStateSection(
    filter: LoanHistoryFilter,
    isDarkMode: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "No loans",
                tint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text =
                    when (filter) {
                        LoanHistoryFilter.ALL -> "No loans found"
                        LoanHistoryFilter.APPROVED -> "No approved loans"
                        LoanHistoryFilter.REJECTED -> "No rejected loans"
                        LoanHistoryFilter.CURRENT -> "No active loans"
                        LoanHistoryFilter.COMPLETED -> "No completed loans"
                    },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                text =
                    when (filter) {
                        LoanHistoryFilter.ALL -> "You haven't applied for any loans yet"
                        else -> "Try selecting a different filter"
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Utility function to format timestamps to readable dates
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun LoanHistoryScreenPreview() {
    SoshoPayTheme {
        // Preview implementation
    }
}
