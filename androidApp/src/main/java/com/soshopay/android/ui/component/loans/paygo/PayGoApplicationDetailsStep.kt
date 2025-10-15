package com.soshopay.android.ui.component.loans.paygo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.domain.model.CashLoanFormData

/**
 * Step 3: Application Details for PayGo Loan
 *
 * Collects usage information, repayment period, and salary band.
 * Pre-populates fields from user profile where available.
 *
 * Following Material Design 3 and SOLID principles.
 *
 * @param usagePerDay Current usage per day value
 * @param repaymentPeriod Current repayment period value
 * @param salaryBand Current salary band value
 * @param formData Cash loan form data for dropdown options
 * @param onUsagePerDayChange Callback for usage per day changes
 * @param onRepaymentPeriodChange Callback for repayment period changes
 * @param onSalaryBandChange Callback for salary band changes
 * @param validationErrors Map of field validation errors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayGoApplicationDetailsStep(
    usagePerDay: String,
    repaymentPeriod: String,
    salaryBand: String,
    formData: CashLoanFormData?,
    onUsagePerDayChange: (String) -> Unit,
    onRepaymentPeriodChange: (String) -> Unit,
    onSalaryBandChange: (String) -> Unit,
    validationErrors: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
    ) {
        // Header
        Text(
            text = "Application Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Provide your usage requirements and financial information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Usage Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usage Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Usage Per Day
                var usageExpanded by remember { mutableStateOf(false) }
                val usageOptions =
                    listOf(
                        "Light Use (1-3 hours/day)",
                        "Moderate Use (4-6 hours/day)",
                        "Heavy Use (7-10 hours/day)",
                        "Continuous Use (10+ hours/day)",
                    )

                ExposedDropdownMenuBox(
                    expanded = usageExpanded,
                    onExpandedChange = { usageExpanded = !usageExpanded },
                ) {
                    OutlinedTextField(
                        value = usagePerDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expected Usage Per Day *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = usageExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("usagePerDay"),
                        supportingText = {
                            validationErrors["usagePerDay"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = usageExpanded,
                        onDismissRequest = { usageExpanded = false },
                    ) {
                        usageOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onUsagePerDayChange(option)
                                    usageExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Financial Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Financial Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Repayment Period
                var periodExpanded by remember { mutableStateOf(false) }
                val repaymentPeriods =
                    formData?.repaymentPeriods ?: listOf(
                        "3 months",
                        "6 months",
                        "12 months",
                        "18 months",
                        "24 months",
                    )

                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = !periodExpanded },
                ) {
                    OutlinedTextField(
                        value = repaymentPeriod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repayment Period *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("repaymentPeriod"),
                        supportingText = {
                            validationErrors["repaymentPeriod"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false },
                    ) {
                        repaymentPeriods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    onRepaymentPeriodChange(period)
                                    periodExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Salary Band
                var salaryExpanded by remember { mutableStateOf(false) }
                val salaryBands =
                    listOf(
                        "Below $300",
                        "$300 - $500",
                        "$500 - $1,000",
                        "$1,000 - $2,000",
                        "Above $2,000",
                    )

                ExposedDropdownMenuBox(
                    expanded = salaryExpanded,
                    onExpandedChange = { salaryExpanded = !salaryExpanded },
                ) {
                    OutlinedTextField(
                        value = salaryBand,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Monthly Salary Band *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = salaryExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("salaryBand"),
                        supportingText = {
                            validationErrors["salaryBand"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = salaryExpanded,
                        onDismissRequest = { salaryExpanded = false },
                    ) {
                        salaryBands.forEach { band ->
                            DropdownMenuItem(
                                text = { Text(band) },
                                onClick = {
                                    onSalaryBandChange(band)
                                    salaryExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info card about salary verification
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your salary band helps us determine appropriate loan terms. This information is kept confidential.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Important notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Notice",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Next Step",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "After completing these details, you'll need to provide a guarantor's information before we can process your application.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
