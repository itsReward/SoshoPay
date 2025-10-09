package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
 * Loan Dashboard Screen following the Activity Diagram flow.
 *
 * Features:
 * - Profile completion check
 * - Cash loan and PayGo loan options
 * - Quick actions for loan history and payments
 * - Error handling with retry functionality
 * - Loading states
 * - Material Design 3 components
 *
 * @param onNavigateToCashLoan Callback for cash loan navigation
 * @param onNavigateToPayGo Callback for PayGo loan navigation
 * @param onNavigateToLoanHistory Callback for loan history navigation
 * @param onNavigateToPayments Callback for payments navigation
 * @param onNavigateToProfile Callback for profile navigation
 * @param viewModel LoanViewModel for state management
 */
@Composable
fun LoanDashboardScreen(
    onNavigateToCashLoan: () -> Unit,
    onNavigateToPayGo: () -> Unit,
    onNavigateToLoanHistory: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: LoanViewModel = koinViewModel(),
) {
    val dashboardState by viewModel.loanDashboardState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is LoanPaymentNavigation.ToCashLoanApplication -> onNavigateToCashLoan()
                is LoanPaymentNavigation.ToPayGoApplication -> onNavigateToPayGo()
                is LoanPaymentNavigation.ToLoanHistory -> onNavigateToLoanHistory()
                is LoanPaymentNavigation.ToPaymentDashboard -> onNavigateToPayments()
                is LoanPaymentNavigation.ToProfile -> onNavigateToProfile()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.White)
                    .verticalScroll(rememberScrollState()),
        ) {
            // Header Section
            LoanDashboardHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Main Content
            if (dashboardState.isLoading) {
                LoadingSection()
            } else if (dashboardState.hasErrors()) {
                ErrorSection(
                    errorMessage = dashboardState.errorMessage!!,
                    onRetry = { viewModel.onEvent(LoanPaymentEvent.LoadLoanDashboard) },
                )
            } else {
                // Loan Options Section
                LoanOptionsSection(
                    onCashLoanClick = onNavigateToCashLoan,
                    onPayGoClick = onNavigateToPayGo,
                    payGoCategories = dashboardState.payGoCategories,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Actions Section
                QuickActionsSection(
                    onLoanHistoryClick = onNavigateToLoanHistory,
                    onPaymentsClick = onNavigateToPayments,
                )
            }

            // Profile Incomplete Dialog
            if (dashboardState.showProfileIncompleteDialog) {
                ProfileIncompleteDialog(
                    onDismiss = { viewModel.onEvent(LoanPaymentEvent.DismissProfileIncompleteDialog) },
                    onNavigateToProfile = {
                        viewModel.onEvent(LoanPaymentEvent.DismissProfileIncompleteDialog)
                        viewModel.onEvent(LoanPaymentEvent.NavigateToProfile)
                    },
                )
            }
        }
    }
}

/**
 * Header section with app branding and title
 */
@Composable
private fun LoanDashboardHeader() {
    val isDarkMode = isSystemInDarkTheme()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.sosho_logo),
            contentDescription = "SoshoPay Logo",
            modifier = Modifier.size(60.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loan Services",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = if (isDarkMode) Color.White else Color.Black,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Choose the loan option that suits your needs",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

/**
 * Loading section with progress indicator
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
                text = "Loading loan options...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * Error section with retry functionality
 */
@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "Something went wrong",
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
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Loan options section with cash loan and PayGo cards
 */
@Composable
private fun LoanOptionsSection(
    onCashLoanClick: () -> Unit,
    onPayGoClick: () -> Unit,
    payGoCategories: List<String>,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Loan Options",
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Cash Loan Card
        LoanOptionCard(
            title = "Cash Loan",
            description = "Get instant cash for your immediate needs",
            icon = Icons.Default.AccountBalance,
            features =
                listOf(
                    "Up to $50,000",
                    "Flexible repayment terms",
                    "Quick approval process",
                    "Competitive interest rates",
                ),
            onClick = onCashLoanClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PayGo Loan Card
        LoanOptionCard(
            title = "PayGo Loan",
            description = "Buy now, pay later for devices and appliances",
            icon = Icons.Default.Phone,
            features =
                listOf(
                    "${payGoCategories.size} categories available",
                    "Weekly payment options",
                    "Home delivery included",
                    "No upfront payment required",
                ),
            onClick = onPayGoClick,
        )
    }
}

/**
 * Individual loan option card
 */
@Composable
private fun LoanOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    features: List<String>,
    onClick: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.onTertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                    tint = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(4.dp)
                                    .background(
                                        if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(2.dp),
                                    ),
                        )

                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Gray,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick actions section with loan history and payments
 */
@Composable
private fun QuickActionsSection(
    onLoanHistoryClick: () -> Unit,
    onPaymentsClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Quick Actions",
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                QuickActionCard(
                    title = "Loan History",
                    description = "View your loan applications and status",
                    icon = Icons.Default.History,
                    onClick = onLoanHistoryClick,
                )
            }

            item {
                QuickActionCard(
                    title = "Make Payment",
                    description = "Pay your loan installments",
                    icon = Icons.Default.Payment,
                    onClick = onPaymentsClick,
                )
            }
        }
    }
}

/**
 * Individual quick action card
 */
@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier =
            Modifier
                .width(180.dp)
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.onTertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
            )

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Profile incomplete dialog
 */
@Composable
private fun ProfileIncompleteDialog(
    onDismiss: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = colorResource(id = R.color.yellow),
                )
                Text(
                    text = "Profile Incomplete",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }
        },
        text = {
            Text(
                text = "Please complete your profile to apply for loans. You'll need to provide personal details, address information, and upload required documents.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onNavigateToProfile,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =  colorResource(id = R.color.yellow),
                    ),
            ) {
                Text("Complete Profile", color = if (isDarkMode) Color.Black else Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Later", color = if (isDarkMode) Color.White else Color.Black)
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
    )
}

@Preview(showBackground = true)
@Composable
fun LoanDashboardScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data - would need to create a preview version
        // that doesn't depend on ViewModel for actual preview
    }
}
