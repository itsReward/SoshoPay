package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.domain.model.CashLoanApplicationStep

/**
 * Step Indicator Component for Cash Loan Application Wizard.
 *
 * Displays a horizontal progress indicator showing:
 * - Current step (highlighted)
 * - Completed steps (with checkmark)
 * - Upcoming steps (inactive)
 * - Progress bar connecting steps
 *
 * Following Material Design 3 principles and SOLID patterns.
 *
 * @param currentStep The current active step
 * @param completedSteps Set of completed step numbers
 * @param modifier Modifier for customization
 */
@Composable
fun StepIndicator(
    currentStep: CashLoanApplicationStep,
    completedSteps: Set<Int> = emptySet(),
    modifier: Modifier = Modifier,
) {
    val steps = CashLoanApplicationStep.values()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = currentStep.getProgress(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEach { step ->
                StepCircle(
                    step = step,
                    isActive = step == currentStep,
                    isCompleted = completedSteps.contains(step.stepNumber),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step titles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            steps.forEach { step ->
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color =
                        if (step == currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else if (completedSteps.contains(step.stepNumber)) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                )
            }
        }
    }
}

/**
 * Individual step circle component.
 */
@Composable
private fun StepCircle(
    step: CashLoanApplicationStep,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.tertiary
                            isCompleted -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
                else -> {
                    Text(
                        text = step.stepNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color =
                            if (isActive) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}
