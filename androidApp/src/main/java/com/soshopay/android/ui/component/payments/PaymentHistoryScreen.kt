package com.soshopay.android.ui.component.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.android.ui.state.LoanPaymentNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.PaymentViewModel
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentStatus
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment History Screen
 *
 * Displays a comprehensive list of all user payments with advanced filtering capabilities.
 * Payments are grouped by loan ID and can be filtered by status, date range, or searched by loan ID.
 *
 * Features:
 * - Grouped payment display by loan ID
 * - Filter by payment status (All, Successful, Failed, Pending)
 * - Date range filtering
 * - Search by loan ID
 * - Payment details modal with download receipt functionality
 * - Pull-to-refresh functionality
 * - Error handling with retry functionality
 * - Empty state messaging
 *
 * Following SOLID Principles:
 * - Single Responsibility: Manages only payment history display
 * - Open/Closed: Can be extended with new filters without modification
 * - Dependency Inversion: Depends on ViewModel abstraction
 *
 * @param onNavigateBack Callback for back navigation
 * @param viewModel PaymentViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = koinViewModel(),
) {
    val historyState by viewModel.paymentHistoryState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.Back -> onNavigateBack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    // Load payment history on first composition
    LaunchedEffect(Unit) {
        viewModel.onEvent(LoanPaymentEvent.LoadPaymentHistory)
    }

    // State for filters and search
    var selectedStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<Payment?>(null) }

    val isDarkMode = isSystemInDarkTheme()
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle pull to refresh
    /*if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.onEvent(LoanPaymentEvent.RefreshPaymentHistory)
        }
    }*/

    // Filter and group payments
    val filteredPayments =
        remember(
            historyState.payments,
            selectedStatus,
            searchQuery,
            startDate,
            endDate,
        ) {
            historyState.payments
                .filter { payment ->
                    // Filter by status
                    val statusMatch = selectedStatus?.let { payment.status == it } ?: true

                    // Filter by search query (loan ID)
                    val searchMatch =
                        searchQuery.isEmpty() ||
                            payment.loanId.contains(searchQuery, ignoreCase = true)

                    // Filter by date range
                    val dateMatch =
                        when {
                            startDate != null && endDate != null ->
                                payment.processedAt >= startDate!! && payment.processedAt <= endDate!!
                            startDate != null -> payment.processedAt >= startDate!!
                            endDate != null -> payment.processedAt <= endDate!!
                            else -> true
                        }

                    statusMatch && searchMatch && dateMatch
                }
        }

    // Group payments by loan ID
    val groupedPayments =
        remember(filteredPayments) {
            filteredPayments.groupBy { it.loanId }
        }

    SoshoPayTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                PaymentHistoryTopBar(
                    onNavigateBack = onNavigateBack,
                    onToggleFilters = { showFilters = !showFilters },
                )

                // Search Bar
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isDarkMode = isDarkMode,
                )

                // Filter Section
                if (showFilters) {
                    FilterSection(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it },
                        startDate = startDate,
                        endDate = endDate,
                        onDateRangeClick = { showDateRangePicker = true },
                        onClearFilters = {
                            selectedStatus = null
                            startDate = null
                            endDate = null
                            searchQuery = ""
                        },
                        isDarkMode = isDarkMode,
                    )
                }

                // Content
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    // .nestedScroll(pullToRefreshState.nestedScrollConnection),
                ) {
                    when {
                        // Loading State
                        historyState.isLoading && historyState.payments.isEmpty() -> {
                            LoadingSection()
                        }

                        // Error State
                        historyState.hasErrors() && historyState.payments.isEmpty() -> {
                            ErrorSection(
                                errorMessage = historyState.errorMessage!!,
                                onRetry = { viewModel.onEvent(LoanPaymentEvent.LoadPaymentHistory) },
                            )
                        }

                        // Empty State
                        groupedPayments.isEmpty() -> {
                            EmptyPaymentHistorySection(hasFilters = selectedStatus != null || searchQuery.isNotEmpty() || startDate != null)
                        }

                        // Success State
                        else -> {
                            PaymentHistoryList(
                                groupedPayments = groupedPayments,
                                onPaymentClick = { selectedPayment = it },
                            )
                        }
                    }

                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }

            // Payment Details Modal
            selectedPayment?.let { payment ->
                PaymentDetailsModal(
                    payment = payment,
                    onDismiss = { selectedPayment = null },
                    onDownloadReceipt = {
                        viewModel.onEvent(LoanPaymentEvent.DownloadReceipt(payment.receiptNumber))
                        selectedPayment = null
                    },
                    isDarkMode = isDarkMode,
                )
            }

            // Date Range Picker Dialog
            if (showDateRangePicker) {
                DateRangePickerDialog(
                    onDismiss = { showDateRangePicker = false },
                    onDateRangeSelected = { start, end ->
                        startDate = start
                        endDate = end
                        showDateRangePicker = false
                    },
                    isDarkMode = isDarkMode,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentHistoryTopBar(
    onNavigateBack: () -> Unit,
    onToggleFilters: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Payment History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate back",
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFilters) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Toggle filters",
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isDarkMode: Boolean,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search by Loan ID...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkMode) MaterialTheme.colorScheme.onTertiary else Color(0xFFF5F5F5),
                unfocusedContainerColor = if (isDarkMode) MaterialTheme.colorScheme.onTertiary else Color(0xFFF5F5F5),
                focusedBorderColor = colorResource(id = R.color.yellow_background),
                unfocusedBorderColor = Color.Transparent,
            ),
    )
}

@Composable
private fun FilterSection(
    selectedStatus: PaymentStatus?,
    onStatusSelected: (PaymentStatus?) -> Unit,
    startDate: Long?,
    endDate: Long?,
    onDateRangeClick: () -> Unit,
    onClearFilters: () -> Unit,
    isDarkMode: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Status Filter Chips
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("All") },
            )
            FilterChip(
                selected = selectedStatus == PaymentStatus.COMPLETED,
                onClick = { onStatusSelected(if (selectedStatus == PaymentStatus.COMPLETED) null else PaymentStatus.COMPLETED) },
                label = { Text("Successful") },
            )
            FilterChip(
                selected = selectedStatus == PaymentStatus.FAILED,
                onClick = { onStatusSelected(if (selectedStatus == PaymentStatus.FAILED) null else PaymentStatus.FAILED) },
                label = { Text("Failed") },
            )
            FilterChip(
                selected = selectedStatus == PaymentStatus.PENDING,
                onClick = { onStatusSelected(if (selectedStatus == PaymentStatus.PENDING) null else PaymentStatus.PENDING) },
                label = { Text("Pending") },
            )
        }

        // Date Range Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onDateRangeClick,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date range",
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text =
                        if (startDate != null && endDate != null) {
                            "${formatDate(startDate)} - ${formatDate(endDate)}"
                        } else {
                            "Select Date Range"
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (selectedStatus != null || startDate != null || endDate != null) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f),
    )
}

@Composable
private fun PaymentHistoryList(
    groupedPayments: Map<String, List<Payment>>,
    onPaymentClick: (Payment) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        groupedPayments.forEach { (loanId, payments) ->
            item {
                LoanGroupHeader(loanId = loanId, paymentCount = payments.size)
            }

            items(payments) { payment ->
                PaymentHistoryCard(
                    payment = payment,
                    onClick = { onPaymentClick(payment) },
                )
            }
        }
    }
}

@Composable
private fun LoanGroupHeader(
    loanId: String,
    paymentCount: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Loan ID: $loanId",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$paymentCount payment${if (paymentCount > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaymentHistoryCard(
    payment: Payment,
    onClick: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.onTertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status Icon
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getStatusColor(payment.status).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getStatusIcon(payment.status),
                    contentDescription = payment.status.name,
                    tint = getStatusColor(payment.status),
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Payment Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatDateTime(payment.processedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Receipt: ${payment.receiptNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(payment.status).copy(alpha = 0.2f),
            ) {
                Text(
                    text = payment.status.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = getStatusColor(payment.status),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailsModal(
    payment: Payment,
    onDismiss: () -> Unit,
    onDownloadReceipt: () -> Unit,
    isDarkMode: Boolean,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Payment Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Badge
            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                color = getStatusColor(payment.status).copy(alpha = 0.2f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = getStatusIcon(payment.status),
                        contentDescription = null,
                        tint = getStatusColor(payment.status),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = payment.status.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = getStatusColor(payment.status),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Information
            DetailRow("Amount", formatCurrency(payment.amount))
            DetailRow("Payment ID", payment.paymentId)
            DetailRow("Receipt Number", payment.receiptNumber)
            DetailRow("Payment Method", payment.method)
            DetailRow("Phone Number", payment.phoneNumber)
            DetailRow("Processed At", formatDateTime(payment.processedAt))

            // Breakdown if available
            if (payment.principal != null || payment.interest != null || payment.penalties != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Payment Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                payment.principal?.let { DetailRow("Principal", formatCurrency(it)) }
                payment.interest?.let { DetailRow("Interest", formatCurrency(it)) }
                payment.penalties?.let { DetailRow("Penalties", formatCurrency(it)) }
            }

            // Failure Reason if failed
            if (payment.status == PaymentStatus.FAILED && payment.failureReason != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Failure Reason",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val failureReason = payment.failureReason
                Text(
                    text = failureReason ?: "Unknown reason of failure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Download Receipt Button
            if (payment.status == PaymentStatus.COMPLETED) {
                Button(
                    onClick = onDownloadReceipt,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.yellow_background),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download Receipt",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long?, Long?) -> Unit,
    isDarkMode: Boolean,
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis,
                    )
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select Date Range",
                    modifier = Modifier.padding(16.dp),
                )
            },
            headline = {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text =
                            dateRangePickerState.selectedStartDateMillis?.let {
                                formatDate(it)
                            } ?: "Start date",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text =
                            dateRangePickerState.selectedEndDateMillis?.let {
                                formatDate(it)
                            } ?: "End date",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            showModeToggle = false,
        )
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.yellow_background),
        )
    }
}

@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error Loading Payments",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.yellow_background),
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Retry",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EmptyPaymentHistorySection(hasFilters: Boolean) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = "No payments",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasFilters) "No payments found" else "No payments yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text =
                if (hasFilters) {
                    "Try adjusting your filters or search query"
                } else {
                    "Your payment history will appear here once you make payments"
                },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Helper Functions
private fun getStatusColor(status: PaymentStatus): Color =
    when (status) {
        PaymentStatus.COMPLETED -> Color(0xFF4CAF50)
        PaymentStatus.FAILED -> Color(0xFFF44336)
        PaymentStatus.PENDING -> Color(0xFFFF9800)
        PaymentStatus.OVERDUE -> Color(0xFFD32F2F)
        PaymentStatus.PROCESSING -> Color(0xFF2196F3)
        PaymentStatus.SUCCESSFUL -> Color(0xFF4CAF50)
        PaymentStatus.CANCELLED -> Color(0xFFF44336)
        PaymentStatus.CURRENT -> Color(0xFFFF9800)
    }

private fun getStatusIcon(status: PaymentStatus): androidx.compose.ui.graphics.vector.ImageVector =
    when (status) {
        PaymentStatus.COMPLETED -> Icons.Default.CheckCircle
        PaymentStatus.FAILED -> Icons.Default.Error
        PaymentStatus.PENDING -> Icons.Default.Schedule
        PaymentStatus.OVERDUE -> Icons.Default.Warning
        PaymentStatus.PROCESSING -> Icons.Default.Sync
        PaymentStatus.SUCCESSFUL -> Icons.Default.CheckCircle
        PaymentStatus.CANCELLED -> Icons.Default.Error
        PaymentStatus.CURRENT -> Icons.Default.Schedule
    }

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
