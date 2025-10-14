package com.soshopay.android.ui.component.loans.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.loans.IndustryDropdown
import com.soshopay.android.ui.state.LoanPaymentEvent

/**
 * Step 2: Income & Employment Screen.
 *
 * Collects:
 * - Monthly income (min $150)
 * - Employer industry (dropdown selection)
 *
 * Pre-populated from user profile if available.
 *
 * @param monthlyIncome Current monthly income
 * @param employerIndustry Current employer industry
 * @param validationErrors Map of field validation errors
 * @param onEvent Callback for handling events
 * @param modifier Modifier for customization
 */
@Composable
fun CashLoanStep2Screen(
    monthlyIncome: String,
    employerIndustry: String,
    validationErrors: Map<String, String>,
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
            text = "Income & Employment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Help us understand your financial situation and employment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Monthly Income
        OutlinedTextField(
            value = monthlyIncome,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateMonthlyIncome(it)) },
            label = { Text("Monthly Income") },
            placeholder = { Text("Enter your monthly income") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Income",
                )
            },
            supportingText = {
                Text(
                    text =
                        validationErrors["monthlyIncome"]
                            ?: "Minimum monthly income: $150",
                )
            },
            isError = validationErrors.containsKey("monthlyIncome"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Employer Industry
        IndustryDropdown(
            selectedIndustry = employerIndustry,
            onIndustrySelected = { onEvent(LoanPaymentEvent.UpdateEmployerIndustry(it)) },
            error = validationErrors["employerIndustry"],
            modifier = Modifier.fillMaxWidth(),
        )

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "🔒 Privacy & Security",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Your income information is encrypted and used only for loan assessment. We never share your financial data with third parties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        // Why we need this information
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
                    text = "ℹ️ Why we ask",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = "We use your income and employment information to:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                BulletPoint("Assess your ability to repay the loan")
                BulletPoint("Determine appropriate loan terms")
                BulletPoint("Offer you the best interest rate possible")
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}
