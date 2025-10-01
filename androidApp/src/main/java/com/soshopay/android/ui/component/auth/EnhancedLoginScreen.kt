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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.state.AuthEvent
import com.soshopay.android.ui.state.AuthNavigation
import com.soshopay.android.ui.theme.DarkTertiaryBackground
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Enhanced Login Screen following Material Design 3 and proper state management.
 *
 * Features:
 * - Form validation with real-time feedback
 * - Loading states with proper UI feedback
 * - Error handling with user-friendly messages
 * - Accessibility support
 * - PIN visibility toggle
 * - Keyboard navigation support
 *
 * @param onNavigateToHome Callback for successful login navigation
 * @param onNavigateToSignUp Callback for sign up navigation
 * @param viewModel AuthViewModel for state management
 */
@Composable
fun EnhancedLoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val loginState by viewModel.loginState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is AuthNavigation.ToHome -> onNavigateToHome()
                is AuthNavigation.ToSignUp -> onNavigateToSignUp()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    var isPinVisible by remember { mutableStateOf(false) }

    SoshoPayTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.White)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .safeDrawingPadding(),
        ) {
            // Logo and App Name
            Image(
                painter = painterResource(id = if (isDarkMode) R.drawable.sosho_logo_dark else R.drawable.sosho_logo),
                contentDescription = "SoshoPay Logo",
                modifier = Modifier.size(120.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SoshoPay",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back! Please sign in to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Form Card
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
                        value = loginState.phoneNumber,
                        onValueChange = { viewModel.onEvent(AuthEvent.UpdatePhoneNumber(it)) },
                        label = { Text("Phone Number") },
                        placeholder = { Text("07XXXXXXXX or +263XXXXXXXX") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Icon",
                            )
                        },
                        isError = !loginState.isPhoneNumberValid,
                        supportingText = {
                            if (loginState.phoneNumberError != null) {
                                Text(
                                    text = loginState.phoneNumberError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    if (isDarkMode) {
                                        colorResource(
                                            id = R.color.yellow,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                focusedLabelColor =
                                    if (isDarkMode) {
                                        colorResource(
                                            id = R.color.yellow,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            ),
                    )

                    // PIN Field
                    OutlinedTextField(
                        value = loginState.pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                viewModel.onEvent(AuthEvent.UpdatePin(it))
                            }
                        },
                        label = { Text("PIN") },
                        placeholder = { Text("Enter your 4-digit PIN") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !loginState.isPinValid,
                        supportingText = {
                            if (loginState.pinError != null) {
                                Text(
                                    text = loginState.pinError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (loginState.isLoginEnabled) {
                                        viewModel.onEvent(AuthEvent.LoginClicked)
                                    }
                                },
                            ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    if (isDarkMode) {
                                        colorResource(
                                            id = R.color.yellow,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                focusedLabelColor =
                                    if (isDarkMode) {
                                        colorResource(
                                            id = R.color.yellow,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login Button
                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.LoginClicked) },
                        enabled = loginState.isLoginEnabled && !loginState.isLoading,
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
                        if (loginState.isLoading) {
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
                                    text = "Signing in...",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                )
                            }
                        } else {
                            Text(
                                text = "Sign In",
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

            // Sign Up Link
            val annotatedString =
                buildAnnotatedString {
                    withStyle(
                        style =
                            SpanStyle(
                                color = if (isDarkMode) Color.White else Color.Black,
                                fontSize = 14.sp,
                            ),
                    ) {
                        append("Don't have an account? ")
                    }

                    pushStringAnnotation(tag = "SignUp", annotation = "Sign up")
                    withStyle(
                        style =
                            SpanStyle(
                                color = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                    ) {
                        append("Sign Up")
                    }
                    pop()
                }

            ClickableText(
                modifier = Modifier.padding(16.dp),
                text = annotatedString,
                onClick = { offset ->
                    annotatedString
                        .getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            viewModel.onEvent(AuthEvent.NavigateToSignUp)
                        }
                },
            )

            // Error Dialog
            if (loginState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AuthEvent.ClearLoginError) },
                    title = {
                        Text(
                            text = "Login Failed",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = if (isDarkMode) Color.White else Color.Black,
                        )
                    },
                    text = {
                        Text(
                            text = loginState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.ClearLoginError) },
                            colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor = if (isDarkMode) Color.White else Color.Black,
                                ),
                        ) {
                            Text("OK")
                        }
                    },
                    containerColor = if (isDarkMode) DarkTertiaryBackground else Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnhancedLoginScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data - would need to create a preview version
        // that doesn't depend on ViewModel for actual preview
    }
}
