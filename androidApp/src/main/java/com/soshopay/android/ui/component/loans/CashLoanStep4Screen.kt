package com.soshopay.android.ui.component.loans.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.domain.model.CashLoanTerms
import java.text.SimpleDateFormat
import java.util.*

/**
 * Step 4: Terms Review Screen.
 *
 * Displays:
 * - Calculate Terms button
 * - Calculated loan terms (if available)
 * - Monthly payment
 * - Interest rate
 * - Total amount
 * - Processing fee
 * - Payment schedule dates
 *
 * @param calculatedTerms Calculated loan terms (null if not calculated)
 * @param isCalculating Whether terms are being calculated
 * @param canCalculate Whether calculation button should be enabled
 * @param onEvent Callback for handling events
 * @param modifier Modifier for customization
 */
@Composable
fun CashLoanStep4Screen(
    calculatedTerms: CashLoanTerms?,
    isCalculating: Boolean,
    canCalculate: Boolean,
    onEvent: (LoanPaymentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = "Review Loan Terms",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Review your personalized loan terms before proceeding.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (calculatedTerms == null) {
            // Calculate Terms Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Calculate",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Ready to see your terms?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Click the button below to calculate your personalized loan terms based on the information you've provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = { onEvent(LoanPaymentEvent.CalculateCashLoanTerms) },
                    enabled = canCalculate && !isCalculating,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculating...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate Loan Terms")
                    }
                }
            }
        } else {
            // Display calculated terms
            Text(
                text = "Your Loan Terms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Main terms card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Monthly Payment (most important)
                    Column {
                        Text(
                            text = "Monthly Payment",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "$${String.format("%.2f", calculatedTerms.monthlyPayment)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Divider()

                    // Other terms
                    TermRow("Interest Rate", "${calculatedTerms.interestRate}%")
                    TermRow("Processing Fee", "$${String.format("%.2f", calculatedTerms.processingFee)}")
                    TermRow("Total Interest", "$${String.format("%.2f", calculatedTerms.getTotalInterest())}")
                    TermRow("Total Amount", "$${String.format("%.2f", calculatedTerms.getTotalCost())}", isBold = true)
                }
            }

            // Payment schedule
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Payment Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "First Payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = formatDate(calculatedTerms.firstPaymentDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Final Payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = formatDate(calculatedTerms.finalPaymentDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "ℹ️ What's Next?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = "If you accept these terms, you'll move to the final confirmation step where you can review everything before submitting your application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // Accept terms button
            Button(
                onClick = { onEvent(LoanPaymentEvent.AcceptTerms) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
            ) {
                Text("Accept Terms & Continue")
            }

            // Recalculate button
            OutlinedButton(
                onClick = { onEvent(LoanPaymentEvent.CalculateCashLoanTerms) },
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Recalculate Terms")
            }
        }
    }
}

/**
 * Individual term row display.
 */
@Composable
private fun TermRow(
    label: String,
    value: String,
    isBold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Formats a timestamp to a readable date.
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
