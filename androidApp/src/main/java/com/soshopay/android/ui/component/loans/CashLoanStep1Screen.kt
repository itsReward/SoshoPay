package com.soshopay.android.ui.component.loans.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.loans.RepaymentPeriodPicker
import com.soshopay.android.ui.state.LoanPaymentEvent

/**
 * Step 1: Loan Details Screen.
 *
 * Collects:
 * - Loan amount (min $100, max $50,000)
 * - Loan purpose (free text)
 * - Repayment period (1-24 months)
 *
 * @param loanAmount Current loan amount
 * @param loanPurpose Current loan purpose
 * @param repaymentPeriod Current repayment period
 * @param validationErrors Map of field validation errors
 * @param onEvent Callback for handling events
 * @param modifier Modifier for customization
 */
@Composable
fun CashLoanStep1Screen(
    loanAmount: String,
    loanPurpose: String,
    repaymentPeriod: String,
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
            text = "Loan Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Tell us how much you need and what you'll use it for.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Loan Amount
        OutlinedTextField(
            value = loanAmount,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateLoanAmount(it)) },
            label = { Text("Loan Amount") },
            placeholder = { Text("Enter amount (min $100, max $50,000)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Amount",
                )
            },
            supportingText = {
                Text(
                    text =
                        validationErrors["loanAmount"]
                            ?: "Minimum: $100 | Maximum: $50,000",
                )
            },
            isError = validationErrors.containsKey("loanAmount"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Loan Purpose
        OutlinedTextField(
            value = loanPurpose,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateLoanPurpose(it)) },
            label = { Text("Loan Purpose") },
            placeholder = { Text("e.g., Business expansion, Home improvement, Education") },
            supportingText = {
                Text(
                    text =
                        validationErrors["loanPurpose"]
                            ?: "Describe what you'll use the loan for",
                )
            },
            isError = validationErrors.containsKey("loanPurpose"),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
        )

        // Repayment Period
        RepaymentPeriodPicker(
            selectedPeriod = repaymentPeriod,
            onPeriodSelected = { onEvent(LoanPaymentEvent.UpdateRepaymentPeriod(it)) },
            error = validationErrors["repaymentPeriod"],
            modifier = Modifier.fillMaxWidth(),
        )

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "💡 Tip",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Be specific about your loan purpose. This helps us process your application faster and may improve your approval chances.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
