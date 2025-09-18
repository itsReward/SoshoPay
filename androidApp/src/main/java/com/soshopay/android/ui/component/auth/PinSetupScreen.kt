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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soshopay.android.R
import com.soshopay.android.ui.state.AuthEvent
import com.soshopay.android.ui.state.AuthNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * PIN Setup Screen for creating a secure 4-digit PIN.
 *
 * Features:
 * - Secure PIN input with confirmation
 * - Real-time validation with visual feedback
 * - PIN visibility toggle
 * - Loading states
 * - Error handling
 * - PIN strength requirements display
 *
 * @param onNavigateToHome Callback for successful PIN setup
 * @param onPopBackStack Callback for back navigation
 * @param viewModel AuthViewModel for state management
 */
@Composable
fun PinSetupScreen(
    onNavigateToHome: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val pinSetupState by viewModel.pinSetupState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is AuthNavigation.ToHome -> onNavigateToHome()
                is AuthNavigation.Back -> onPopBackStack()
                else -> { /* Handle other navigation events */ }
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    var isPinVisible by remember { mutableStateOf(false) }
    var isConfirmPinVisible by remember { mutableStateOf(false) }

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
                text = "Create Your PIN",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a secure 4-digit PIN to protect your account",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Setup Card
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // PIN Security Guidelines
                    SecurityGuidelinesSection()

                    Spacer(modifier = Modifier.height(8.dp))

                    // PIN Input Field
                    OutlinedTextField(
                        value = pinSetupState.pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                viewModel.onEvent(AuthEvent.UpdateSetupPin(it))
                            }
                        },
                        label = { Text("Enter PIN") },
                        placeholder = { Text("4-digit PIN") },
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
                        isError = !pinSetupState.isPinValid,
                        supportingText = {
                            if (pinSetupState.pinError != null) {
                                Text(
                                    text = pinSetupState.pinError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
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
                            ),
                    )

                    // Confirm PIN Input Field
                    OutlinedTextField(
                        value = pinSetupState.confirmPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                viewModel.onEvent(AuthEvent.UpdateConfirmPin(it))
                            }
                        },
                        label = { Text("Confirm PIN") },
                        placeholder = { Text("Re-enter your PIN") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPinVisible = !isConfirmPinVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isConfirmPinVisible) "Hide PIN" else "Show PIN",
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !pinSetupState.isConfirmPinValid,
                        supportingText = {
                            if (pinSetupState.confirmPinError != null) {
                                Text(
                                    text = pinSetupState.confirmPinError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else if (pinSetupState.pin.isNotEmpty() && pinSetupState.confirmPin.isNotEmpty() &&
                                pinSetupState.pinsMatch()
                            ) {
                                Text(
                                    text = "PINs match ✓",
                                    color = MaterialTheme.colorScheme.primary,
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
                                    if (pinSetupState.isPinSetupEnabled) {
                                        viewModel.onEvent(AuthEvent.SetupPinClicked)
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
                            ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Create PIN Button
                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.SetupPinClicked) },
                        enabled = pinSetupState.isPinSetupEnabled && !pinSetupState.isLoading,
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
                        if (pinSetupState.isLoading) {
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
                                    text = "Creating Account...",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                )
                            }
                        } else {
                            Text(
                                text = "Create PIN & Complete Setup",
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
            if (pinSetupState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AuthEvent.ClearPinSetupError) },
                    title = {
                        Text(
                            text = "PIN Setup Failed",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    },
                    text = {
                        Text(
                            text = pinSetupState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.ClearPinSetupError) },
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
 * Security guidelines section for PIN creation
 */
@Composable
private fun SecurityGuidelinesSection() {
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "PIN Security Tips:",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (isDarkMode) Color.White else Color.Black,
            )

            SecurityTip("Use a unique 4-digit combination")
            SecurityTip("Avoid obvious patterns (1234, 1111)")
            SecurityTip("Don't share your PIN with anyone")
            SecurityTip("Your PIN will secure all transactions")
        }
    }
}

/**
 * Individual security tip item
 */
@Composable
private fun SecurityTip(text: String) {
    val isDarkMode = isSystemInDarkTheme()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Gray,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PinSetupScreenPreview() {
    SoshoPayTheme {
        // Preview with mock data
    }
}
