package com.soshopay.android.ui.component.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.state.AuthEvent
import com.soshopay.android.ui.state.AuthNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Enhanced Sign Up Screen with comprehensive validation and user experience.
 *
 * Features:
 * - Phone number validation with Zimbabwe format support
 * - Real-time validation feedback
 * - Loading states with proper UI feedback
 * - Error handling with user-friendly messages
 * - User exists dialog with navigation to login
 * - Back navigation support
 * - Accessibility support
 *
 * @param onNavigateToOtpVerification Callback for OTP verification navigation
 * @param onNavigateToLogin Callback for login navigation
 * @param onPopBackStack Callback for back navigation
 * @param viewModel AuthViewModel for state management
 */
@Composable
fun EnhancedSignUpScreen(
    onNavigateToOtpVerification: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val signUpState by viewModel.signUpState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is AuthNavigation.ToOtpVerification -> onNavigateToOtpVerification()
                is AuthNavigation.ToLogin -> onNavigateToLogin()
                is AuthNavigation.Back -> onPopBackStack()
                else -> { /* Handle other navigation events */ }
            }
        }
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

            // Logo and App Name
            Image(
                painter = painterResource(id = if (isDarkMode) R.drawable.sosho_logo_dark else R.drawable.sosho_logo),
                contentDescription = "SoshoPay Logo",
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your phone number to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Form Card
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Phone Number Field
                    OutlinedTextField(
                        value = signUpState.phoneNumber,
                        onValueChange = { viewModel.onEvent(AuthEvent.UpdateSignUpPhoneNumber(it)) },
                        label = { Text("Phone Number") },
                        placeholder = { Text("07XXXXXXXX or +263XXXXXXXX") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Icon",
                            )
                        },
                        isError = !signUpState.isPhoneNumberValid,
                        supportingText = {
                            if (signUpState.phoneNumberError != null) {
                                Text(
                                    text = signUpState.phoneNumberError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text(
                                    text = "We'll send you an OTP to verify your number",
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (signUpState.isSignUpEnabled) {
                                        viewModel.onEvent(AuthEvent.SignUpClicked)
                                    }
                                },
                            ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    colorResource(
                                        id = R.color.yellow,
                                    ),
                                focusedLabelColor =
                                    colorResource(
                                        id = R.color.yellow,
                                    ),
                                focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                errorTextColor = colorResource(R.color.ecocash_red),
                            ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Continue Button
                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.SignUpClicked) },
                        enabled = signUpState.isSignUpEnabled && !signUpState.isLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.yellow),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                            ),
                        shape = RoundedCornerShape(12.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                    ) {
                        if (signUpState.isLoading) {
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
                                    text = "Sending OTP...",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                )
                            }
                        } else {
                            Text(
                                text = "Continue",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            val annotatedString =
                buildAnnotatedString {
                    withStyle(
                        style =
                            SpanStyle(
                                color = if (isDarkMode) Color.White else Color.Black,
                                fontSize = 14.sp,
                            ),
                    ) {
                        append("Already have an account? ")
                    }

                    pushStringAnnotation(tag = "Login", annotation = "Login")
                    withStyle(
                        style =
                            SpanStyle(
                                color = colorResource(id = R.color.yellow),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                    ) {
                        append("Sign In")
                    }
                    pop()
                }

            ClickableText(
                modifier = Modifier.padding(16.dp),
                text = annotatedString,
                onClick = { offset ->
                    annotatedString
                        .getStringAnnotations(tag = "Login", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            viewModel.onEvent(AuthEvent.NavigateToLogin)
                        }
                },
            )

            // User Already Exists Dialog
            if (signUpState.showUserExistsDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AuthEvent.DismissUserExistsDialog) },
                    title = {
                        Text(
                            text = "Account Exists",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    },
                    text = {
                        Text(
                            text = "An account with this phone number already exists. Would you like to sign in instead?",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(AuthEvent.DismissUserExistsDialog)
                                viewModel.onEvent(AuthEvent.NavigateToLogin)
                            },
                        ) {
                            Text("Sign In")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.DismissUserExistsDialog) },
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                )
            }

            // Error Dialog
            if (signUpState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AuthEvent.ClearSignUpError) },
                    title = {
                        Text(
                            text = "Sign Up Error",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = if (isDarkMode) Color.White else Color.Black,
                        )
                    },
                    text = {
                        Text(
                            text = signUpState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) Color.White else Color.Black,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.ClearSignUpError) },
                        ) {
                            Text("OK", color = if (isDarkMode) Color.White else Color.Black)
                        }
                    },
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnhancedSignUpScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data - would need to create a preview version
        // that doesn't depend on ViewModel for actual preview
    }
}
