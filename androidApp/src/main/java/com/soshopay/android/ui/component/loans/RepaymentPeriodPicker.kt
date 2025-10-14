package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Repayment Period Picker Component.
 *
 * Allows users to select a repayment period from 1 to 24 months.
 * Displays options in a grid format for easy selection.
 *
 * @param selectedPeriod Currently selected period (e.g., "6 months", "1 year")
 * @param onPeriodSelected Callback when a period is selected
 * @param error Error message to display
 * @param modifier Modifier for customization
 */
@Composable
fun RepaymentPeriodPicker(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    // Generate period options
    val periodOptions =
        remember {
            buildList {
                // 1-11 months
                for (i in 1..11) {
                    add(
                        PeriodOption(
                            value = "$i ${if (i == 1) "month" else "months"}",
                            displayText = "$i mo",
                            months = i,
                        ),
                    )
                }
                // 1-2 years
                add(PeriodOption("1 year", "1 yr", 12))
                add(PeriodOption("18 months", "18 mo", 18))
                add(PeriodOption("2 years", "2 yrs", 24))
            }
        }

    Column(modifier = modifier) {
        Text(
            text = "Repayment Period",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Grid of period options
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            periodOptions.chunked(4).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowOptions.forEach { option ->
                        PeriodChip(
                            option = option,
                            isSelected = selectedPeriod == option.value,
                            onSelected = { onPeriodSelected(option.value) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Fill remaining space if row is incomplete
                    repeat(4 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Error message
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        // Selected period summary
        if (selectedPeriod.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected: $selectedPeriod",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Individual period chip/button.
 */
@Composable
private fun PeriodChip(
    option: PeriodOption,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.displayText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        },
        modifier = modifier.height(40.dp),
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

/**
 * Data class representing a period option.
 */
private data class PeriodOption(
    val value: String, // Value to store (e.g., "6 months")
    val displayText: String, // Text to display (e.g., "6 mo")
    val months: Int, // Number of months
)
