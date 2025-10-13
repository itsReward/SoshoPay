// File: androidApp/src/main/java/com/soshopay/android/ui/component/payments/PaymentModals.kt

package com.soshopay.android.ui.component.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.soshopay.android.R
import com.soshopay.android.ui.state.PaymentResult
import com.soshopay.domain.model.PaymentMethodInfo
import java.text.NumberFormat
import java.util.*

/**
 * Full Screen Modal for Payment Method Selection
 */
@Composable
fun PaymentMethodSelectionModal(
    paymentMethods: List<PaymentMethodInfo>,
    selectedMethod: PaymentMethodInfo?,
    onMethodSelected: (PaymentMethodInfo) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDarkMode: Boolean,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Select Payment Method",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkMode) Color.White else Color.Black,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Methods List
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    paymentMethods.forEach { method ->
                        PaymentMethodCard(
                            method = method,
                            isSelected = selectedMethod?.id == method.id,
                            onClick = { onMethodSelected(method) },
                            isDarkMode = isDarkMode,
                        )
                    }
                }

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    enabled = selectedMethod != null,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            disabledContainerColor = Color.Gray,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Continue",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * Payment Method Card
 */
@Composable
private fun PaymentMethodCard(
    method: PaymentMethodInfo,
    isSelected: Boolean,
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
                containerColor =
                    if (isSelected) {
                        if (isDarkMode) {
                            colorResource(id = R.color.yellow).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        }
                    } else {
                        if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White
                    },
            ),
        border =
            if (isSelected) {
                androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                )
            } else {
                null
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow).copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = method.name,
                        tint =
                            if (isDarkMode) {
                                colorResource(id = R.color.yellow)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        modifier = Modifier.size(28.dp),
                    )
                }

                // Method Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = method.name,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )

                    if (method.processingTime != null) {
                        Text(
                            text = "Processing time: ${method.processingTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        )
                    }
                }
            }

            // Selection Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint =
                        if (isDarkMode) {
                            colorResource(id = R.color.yellow)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

/**
 * Full Screen Modal for Phone Number Input
 */
@Composable
fun PhoneInputModal(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    paymentAmount: String,
    onPaymentAmountChange: (String) -> Unit,
    validationErrors: Map<String, String>,
    onDismiss: () -> Unit,
    onMakePayment: () -> Unit,
    isProcessing: Boolean,
    isDarkMode: Boolean,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Enter Payment Details",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isDarkMode) Color.White else Color.Black,
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkMode) Color.White else Color.Black,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Amount Field
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Payment Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontWeight = FontWeight.Medium,
                    )

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = onPaymentAmountChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter amount") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Amount",
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                        isError = validationErrors.containsKey("paymentAmount"),
                        supportingText = {
                            validationErrors["paymentAmount"]?.let { error ->
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    if (isDarkMode) {
                                        colorResource(id = R.color.yellow)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            ),
                    )
                }

                // Phone Number Field
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "EcoCash Phone Number",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontWeight = FontWeight.Medium,
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., 0771234567") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                            ),
                        isError = validationErrors.containsKey("phoneNumber"),
                        supportingText = {
                            validationErrors["phoneNumber"]?.let { error ->
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    if (isDarkMode) {
                                        colorResource(id = R.color.yellow)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            ),
                    )
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (isDarkMode) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                },
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint =
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        )
                        Text(
                            text = "You will receive an EcoCash prompt on your phone to authorize this payment. Processing typically takes 2-5 minutes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color.Black,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Make Payment Button
                Button(
                    onClick = onMakePayment,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    enabled =
                        !isProcessing &&
                            phoneNumber.isNotEmpty() &&
                            paymentAmount.isNotEmpty() &&
                            validationErrors.isEmpty(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            disabledContainerColor = Color.Gray,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text(
                            text = "Make Payment",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Screen Modal for Payment Result (Success/Failure)
 */
@Composable
fun PaymentResultModal(
    paymentResult: PaymentResult,
    onViewReceipt: () -> Unit,
    onReturnToDashboard: () -> Unit,
    isDarkMode: Boolean,
) {
    Dialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Result Icon
                Box(
                    modifier =
                        Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                if (paymentResult.isSuccessful) {
                                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector =
                            if (paymentResult.isSuccessful) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Error
                            },
                        contentDescription = if (paymentResult.isSuccessful) "Success" else "Failed",
                        tint =
                            if (paymentResult.isSuccessful) {
                                Color(0xFF4CAF50)
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        modifier = Modifier.size(64.dp),
                    )
                }

                // Result Title
                Text(
                    text = if (paymentResult.isSuccessful) "Payment Successful!" else "Payment Failed",
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (isDarkMode) Color.White else Color.Black,
                    textAlign = TextAlign.Center,
                )

                // Result Message
                Text(
                    text = paymentResult.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Gray,
                    textAlign = TextAlign.Center,
                )

                // Transaction ID
                if (paymentResult.transactionId != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isDarkMode) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        Color(0xFFF5F5F5)
                                    },
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Transaction ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            )
                            Text(
                                text = paymentResult.transactionId,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                                color = if (isDarkMode) Color.White else Color.Black,
                            )
                        }
                    }
                }

                // Failure Reason (if failed)
                if (!paymentResult.isSuccessful && paymentResult.failureReason != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = paymentResult.failureReason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkMode) Color.White else Color.Black,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // View Receipt Button (only for successful payments)
                    if (paymentResult.isSuccessful && paymentResult.receiptNumber != null) {
                        Button(
                            onClick = onViewReceipt,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (isDarkMode) {
                                            colorResource(id = R.color.yellow)
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Receipt",
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = Color.White,
                            )
                        }
                    }

                    // Return to Dashboard Button
                    OutlinedButton(
                        onClick = onReturnToDashboard,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        border =
                            androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Return to Dashboard",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color =
                                if (isDarkMode) {
                                    colorResource(id = R.color.yellow)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        )
                    }
                }
            }
        }
    }
}

// Utility function to format currency
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
