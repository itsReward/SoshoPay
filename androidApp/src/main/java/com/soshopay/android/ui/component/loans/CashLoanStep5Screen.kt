package com.soshopay.android.ui.component.loans.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.CollateralDocument

/**
 * Step 5: Final Confirmation Screen.
 *
 * Displays:
 * - Summary of all application details
 * - Terms summary
 * - Terms acceptance checkbox
 * - Submit button
 *
 * @param loanAmount Loan amount
 * @param loanPurpose Loan purpose
 * @param repaymentPeriod Repayment period
 * @param monthlyIncome Monthly income
 * @param employerIndustry Employer industry
 * @param collateralType Collateral type
 * @param collateralValue Collateral value
 * @param collateralDocuments List of collateral documents
 * @param calculatedTerms Calculated loan terms
 * @param isSubmitting Whether application is being submitted
 * @param onEvent Callback for handling events
 * @param modifier Modifier for customization
 */
@Composable
fun CashLoanStep5Screen(
    loanAmount: String,
    loanPurpose: String,
    repaymentPeriod: String,
    monthlyIncome: String,
    employerIndustry: String,
    collateralType: String,
    collateralValue: String,
    collateralDocuments: List<CollateralDocument>,
    calculatedTerms: CashLoanTerms?,
    isSubmitting: Boolean,
    onEvent: (LoanPaymentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var termsAccepted by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Final Confirmation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = "Review your application details before submitting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Loan Details Summary
        SummaryCard(title = "Loan Details") {
            SummaryRow("Loan Amount", "$$loanAmount")
            SummaryRow("Purpose", loanPurpose)
            SummaryRow("Repayment Period", repaymentPeriod)
        }

        // Income & Employment Summary
        SummaryCard(title = "Income & Employment") {
            SummaryRow("Monthly Income", "$$monthlyIncome")
            SummaryRow("Industry", employerIndustry)
        }

        // Collateral Summary
        SummaryCard(title = "Collateral Information") {
            SummaryRow("Type", collateralType)
            SummaryRow("Value", "$$collateralValue")
            SummaryRow("Documents", "${collateralDocuments.size} uploaded")
        }

        // Terms Summary
        if (calculatedTerms != null) {
            SummaryCard(title = "Loan Terms", isPrimary = true) {
                SummaryRow(
                    "Monthly Payment",
                    "$${String.format("%.2f", calculatedTerms.monthlyPayment)}",
                    isBold = true,
                )
                SummaryRow("Interest Rate", "${calculatedTerms.interestRate}%")
                SummaryRow("Processing Fee", "$${String.format("%.2f", calculatedTerms.processingFee)}")
                SummaryRow(
                    "Total Cost",
                    "$${String.format("%.2f", calculatedTerms.getTotalCost())}",
                    isBold = true,
                )
            }
        }

        // Terms acceptance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Text(
                    text = "By submitting this application, I confirm that:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CheckItem("All information provided is accurate and complete")
                    CheckItem("I understand the loan terms and repayment obligations")
                    CheckItem("I agree to the terms and conditions")
                    CheckItem("I authorize credit checks and verification")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                    )
                    Text(
                        text = "I have read and accept the terms and conditions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        // Important notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "⚠️ Important Notice",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = "Once submitted, you cannot modify your application. Please ensure all information is correct before proceeding.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        // Submit button
        Button(
            onClick = {
                if (termsAccepted) {
                    onEvent(LoanPaymentEvent.SubmitCashLoanApplication)
                }
            },
            enabled = termsAccepted && !isSubmitting,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submitting Application...")
            } else {
                Text("Submit Application")
            }
        }
    }
}

/**
 * Summary card component.
 */
@Composable
private fun SummaryCard(
    title: String,
    isPrimary: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            if (isPrimary) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color =
                    if (isPrimary) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            Divider()
            content()
        }
    }
}

/**
 * Summary row component.
 */
@Composable
private fun SummaryRow(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/**
 * Check item component.
 */
@Composable
private fun CheckItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
