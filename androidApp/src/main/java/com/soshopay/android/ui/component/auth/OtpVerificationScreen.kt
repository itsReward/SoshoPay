package com.soshopay.android.ui.component.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.state.AuthEvent
import com.soshopay.android.ui.state.AuthNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * OTP Verification Screen with 6-digit code input and timer functionality.
 *
 * Features:
 * - 6-digit OTP input with individual digit boxes
 * - Countdown timer with auto-resend capability
 * - Attempt tracking with max limits
 * - Real-time validation
 * - Loading states
 * - Error handling
 * - Resend OTP functionality
 *
 * @param onNavigateToPinSetup Callback for PIN setup navigation
 * @param onPopBackStack Callback for back navigation
 * @param viewModel AuthViewModel for state management
 */
@Composable
fun OtpVerificationScreen(
    onNavigateToPinSetup: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val otpState by viewModel.otpVerificationState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is AuthNavigation.ToPinSetup -> onNavigateToPinSetup()
                is AuthNavigation.Back -> onPopBackStack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    // Auto-focus on the OTP input when screen loads
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure UI is ready
        focusRequester.requestFocus()
    }

    val isDarkMode = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    SoshoPayTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier =
                Modifier
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.White)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .safeDrawingPadding(),
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                IconButton(onClick = { viewModel.onEvent(AuthEvent.NavigateBack) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color.Black,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logo and Title
            Image(
                painter = painterResource(id = R.drawable.sosho_logo),
                contentDescription = "SoshoPay Logo",
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verify Your Number",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )

            Text(
                text = otpState.phoneNumber,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Verification Card
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // OTP Input Boxes
                    OtpInputField(
                        otpCode = otpState.otpCode,
                        onOtpChange = { viewModel.onEvent(AuthEvent.UpdateOtpCode(it)) },
                        isError = !otpState.isOtpValid,
                        focusRequester = focusRequester,
                    )

                    // Error message
                    if (otpState.otpError != null) {
                        Text(
                            text = otpState.otpError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Timer and Resend
                    TimerSection(
                        timeRemaining = otpState.timeRemaining,
                        canResend = otpState.canResend,
                        isLoading = otpState.isLoading,
                        onResendClicked = { viewModel.onEvent(AuthEvent.ResendOtpClicked) },
                    )

                    // Attempts Remaining
                    if (otpState.attemptsRemaining < 3) {
                        Text(
                            text = "Attempts remaining: ${otpState.attemptsRemaining}",
                            color =
                                if (otpState.attemptsRemaining <=
                                    1
                                ) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Verify Button
                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.VerifyOtpClicked) },
                        enabled = otpState.isVerificationEnabled && !otpState.isLoading && !otpState.isMaxAttemptsReached(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                            ),
                        shape = RoundedCornerShape(12.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                    ) {
                        if (otpState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verifying...",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                )
                            }
                        } else {
                            Text(
                                text = "Verify Code",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                            )
                        }
                    }
                }
            }

            // Error Dialog
            if (otpState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AuthEvent.ClearOtpError) },
                    title = {
                        Text(
                            text = "Verification Failed",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    },
                    text = {
                        Text(
                            text = otpState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.ClearOtpError) },
                        ) {
                            Text("OK")
                        }
                    },
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                )
            }
        }
    }
}

/**
 * Custom OTP input field with 6 individual digit boxes
 */
@Composable
private fun OtpInputField(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean = false,
    focusRequester: FocusRequester,
) {
    val isDarkMode = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hidden text field for actual input
        BasicTextField(
            value = otpCode,
            onValueChange = onOtpChange,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
            modifier =
                Modifier
                    .focusRequester(focusRequester)
                    .size(0.dp),
            // Hide the actual text field
            textStyle = TextStyle(color = Color.Transparent),
        )

        // Visual OTP boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(6) { index ->
                OtpDigitBox(
                    digit = otpCode.getOrNull(index)?.toString() ?: "",
                    isActive = index == otpCode.length,
                    isError = isError,
                    onClick = { focusRequester.requestFocus() },
                )
            }
        }
    }
}

/**
 * Individual digit box for OTP input
 */
@Composable
private fun OtpDigitBox(
    digit: String,
    isActive: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Gray.copy(alpha = 0.1f)
                    },
                ).border(
                    width = if (isActive) 2.dp else 1.dp,
                    color =
                        when {
                            isError -> MaterialTheme.colorScheme.error
                            isActive -> if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary
                            else -> Color.Gray.copy(alpha = 0.3f)
                        },
                    shape = RoundedCornerShape(8.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = if (isDarkMode) Color.White else Color.Black,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Timer section with countdown and resend functionality
 */
@Composable
private fun TimerSection(
    timeRemaining: Int,
    canResend: Boolean,
    isLoading: Boolean,
    onResendClicked: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (timeRemaining > 0) {
            Text(
                text = "Resend code in ${String.format("%02d:%02d", timeRemaining / 60, timeRemaining % 60)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = "Didn't receive the code?",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )
        }

        if (canResend) {
            TextButton(
                onClick = onResendClicked,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sending...")
                    }
                } else {
                    Text(
                        text = "Resend Code",
                        color = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data
    }
}
