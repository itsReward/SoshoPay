package com.soshopay.android.ui.component.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soshopay.android.R
import com.soshopay.android.ui.state.ProfileEvent
import com.soshopay.android.ui.state.ProfileNavigation
import com.soshopay.android.ui.theme.SoshoPayTheme
import com.soshopay.android.ui.viewmodel.ProfileViewModel
import com.soshopay.domain.model.VerificationStatus
import org.koin.androidx.compose.koinViewModel

/**
 * Profile Screen with view/edit capabilities for all profile information.
 *
 * Features:
 * - View/Edit personal details
 * - View/Edit address
 * - Upload/View profile picture
 * - Upload/View documents (National ID, Proof of Residence)
 * - View/Edit Next of Kin
 * - Change client type
 * - Logout functionality
 *
 * @param onNavigateBack Callback for back navigation
 * @param onNavigateToLogin Callback for login navigation after logout
 * @param viewModel ProfileViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val profileState by viewModel.profileScreenState.collectAsState()
    val personalDetailsState by viewModel.personalDetailsEditState.collectAsState()
    val addressState by viewModel.addressEditState.collectAsState()
    val nextOfKinState by viewModel.nextOfKinEditState.collectAsState()
    val profilePictureState by viewModel.profilePictureState.collectAsState()
    val documentState by viewModel.documentUploadState.collectAsState()
    val navigationEvents = viewModel.navigationEvents

    // Handle navigation events
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            when (event) {
                is ProfileNavigation.ToLogin -> onNavigateToLogin()
                is ProfileNavigation.Back -> onNavigateBack()
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current

    // Image picker for profile picture
    val profileImagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val fileName = "profile_${System.currentTimeMillis()}.jpg"
                    viewModel.onEvent(ProfileEvent.UploadProfilePicture(bytes, fileName))
                }
            }
        }

    // Document pickers
    val nationalIdPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val fileName = "national_id_${System.currentTimeMillis()}.pdf"
                    viewModel.onEvent(ProfileEvent.UploadNationalId(bytes, fileName))
                }
            }
        }

    val proofOfResidencePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val fileName = "proof_of_residence_${System.currentTimeMillis()}.pdf"
                    viewModel.onEvent(ProfileEvent.UploadProofOfResidence(bytes, fileName))
                }
            }
        }

    SoshoPayTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onEvent(ProfileEvent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                            )
                        }
                    },
                    actions = {
                        if (!profileState.isEditMode) {
                            IconButton(onClick = { viewModel.onEvent(ProfileEvent.ToggleEditMode) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(ProfileEvent.ShowLogoutDialog) }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = if (isDarkMode) MaterialTheme.colorScheme.primary else Color.White,
                            titleContentColor = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                        ),
                )
            },
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.secondary else Color.White,
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                // Profile Picture Section
                ProfilePictureSection(
                    imageUrl = profilePictureState.imageUrl,
                    isUploading = profilePictureState.isUploading,
                    uploadProgress = profilePictureState.uploadProgress,
                    onUploadClick = { profileImagePicker.launch("image/*") },
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // User Info Header
                profileState.user?.let { user ->
                    Text(
                        text = user.getFullName(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user.phoneNumber,
                        fontSize = 16.sp,
                        color = if (isDarkMode) Color.Gray else Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Verification Status
                    VerificationStatusChip(
                        status = user.verificationStatus,
                        isDarkMode = isDarkMode,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Personal Details Section
                PersonalDetailsSection(
                    user = profileState.user,
                    editState = personalDetailsState,
                    isEditMode = profileState.isEditMode,
                    onEvent = viewModel::onEvent,
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Address Section
                AddressSection(
                    user = profileState.user,
                    editState = addressState,
                    isEditMode = profileState.isEditMode,
                    onEvent = viewModel::onEvent,
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Documents Section
                DocumentsSection(
                    documentState = documentState,
                    onUploadNationalId = { nationalIdPicker.launch("application/pdf,image/*") },
                    onUploadProofOfResidence = { proofOfResidencePicker.launch("application/pdf,image/*") },
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Next of Kin Section
                NextOfKinSection(
                    user = profileState.user,
                    editState = nextOfKinState,
                    isEditMode = profileState.isEditMode,
                    onEvent = viewModel::onEvent,
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Client Type Section
                ClientTypeSection(
                    user = profileState.user,
                    onChangeClientType = { viewModel.onEvent(ProfileEvent.ShowClientTypeDialog) },
                    isDarkMode = isDarkMode,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save/Cancel Buttons in Edit Mode
                if (profileState.isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(ProfileEvent.CancelEdit) },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                                ),
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { viewModel.onEvent(ProfileEvent.SavePersonalDetails) },
                            modifier = Modifier.weight(1f),
                            enabled = personalDetailsState.isSaveEnabled,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.sosho_blue),
                                    contentColor = Color.White,
                                ),
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }

            // Loading Overlay
            if (profileState.isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.sosho_blue))
                }
            }

            // Logout Confirmation Dialog
            if (profileState.showLogoutDialog) {
                LogoutConfirmationDialog(
                    onConfirm = { viewModel.onEvent(ProfileEvent.ConfirmLogout) },
                    onDismiss = { viewModel.onEvent(ProfileEvent.DismissLogoutDialog) },
                    isDarkMode = isDarkMode,
                )
            }

            // Client Type Change Dialog
            if (profileState.showClientTypeDialog) {
                ClientTypeDialog(
                    availableTypes = profileState.availableClientTypes,
                    onSelectType = { type -> viewModel.onEvent(ProfileEvent.RequestClientTypeChange(type)) },
                    onDismiss = { viewModel.onEvent(ProfileEvent.DismissClientTypeDialog) },
                    isDarkMode = isDarkMode,
                )
            }

            // Error Snackbar
            profileState.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    // Show error snackbar
                    kotlinx.coroutines.delay(3000)
                    viewModel.onEvent(ProfileEvent.ClearError)
                }
            }
        }
    }
}

@Composable
fun ProfilePictureSection(
    imageUrl: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    onUploadClick: () -> Unit,
    isDarkMode: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                        shape = CircleShape,
                    ).background(if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile",
                    modifier = Modifier.size(60.dp),
                    tint = if (isDarkMode) Color.White else Color.Gray,
                )
            }

            if (isUploading) {
                CircularProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.sosho_blue),
                )
            }
        }

        // Upload Button
        FloatingActionButton(
            onClick = onUploadClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 80.dp, bottom = 0.dp)
                    .size(40.dp),
            containerColor = colorResource(id = R.color.sosho_blue),
            contentColor = Color.White,
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Upload Picture",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun VerificationStatusChip(
    status: VerificationStatus,
    isDarkMode: Boolean,
) {
    val (text, color) =
        when (status) {
            VerificationStatus.VERIFIED -> "Verified" to Color.Green
            VerificationStatus.PENDING -> "Pending Verification" to Color.Yellow
            VerificationStatus.UNVERIFIED -> "Unverified" to Color.Red
            VerificationStatus.REJECTED -> "Rejected" to Color.Red
        }

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f),
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PersonalDetailsSection(
    user: com.soshopay.domain.model.User?,
    editState: com.soshopay.android.ui.state.PersonalDetailsEditState,
    isEditMode: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    isDarkMode: Boolean,
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Personal Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                // Edit Mode - Text Fields
                OutlinedTextField(
                    value = editState.firstName,
                    onValueChange = { onEvent(ProfileEvent.UpdateFirstName(it)) },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.firstNameError != null,
                    supportingText = editState.firstNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.lastName,
                    onValueChange = { onEvent(ProfileEvent.UpdateLastName(it)) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.lastNameError != null,
                    supportingText = editState.lastNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.gender,
                    onValueChange = { onEvent(ProfileEvent.UpdateGender(it)) },
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.genderError != null,
                    supportingText = editState.genderError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.nationality,
                    onValueChange = { onEvent(ProfileEvent.UpdateNationality(it)) },
                    label = { Text("Nationality") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.nationalityError != null,
                    supportingText = editState.nationalityError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.occupation,
                    onValueChange = { onEvent(ProfileEvent.UpdateOccupation(it)) },
                    label = { Text("Occupation") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.occupationError != null,
                    supportingText = editState.occupationError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.monthlyIncome,
                    onValueChange = { onEvent(ProfileEvent.UpdateMonthlyIncome(it)) },
                    label = { Text("Monthly Income") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.monthlyIncomeError != null,
                    supportingText = editState.monthlyIncomeError?.let { { Text(it) } },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )
            } else {
                // View Mode - Read-only display
                user?.personalDetails?.let { details ->
                    ProfileInfoRow("First Name", details.firstName, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Last Name", details.lastName, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Gender", details.gender, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Nationality", details.nationality, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Occupation", details.occupation, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Monthly Income", "${details.monthlyIncome}", isDarkMode)
                } ?: Text(
                    text = "No personal details available",
                    color = if (isDarkMode) Color.Gray else Color.LightGray,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun AddressSection(
    user: com.soshopay.domain.model.User?,
    editState: com.soshopay.android.ui.state.AddressEditState,
    isEditMode: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    isDarkMode: Boolean,
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Address",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                OutlinedTextField(
                    value = editState.streetAddress,
                    onValueChange = { onEvent(ProfileEvent.UpdateStreetAddress(it)) },
                    label = { Text("Street Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.streetAddressError != null,
                    supportingText = editState.streetAddressError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.suburb,
                    onValueChange = { onEvent(ProfileEvent.UpdateSuburb(it)) },
                    label = { Text("Suburb") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.suburbError != null,
                    supportingText = editState.suburbError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.city,
                    onValueChange = { onEvent(ProfileEvent.UpdateCity(it)) },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.cityError != null,
                    supportingText = editState.cityError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.province,
                    onValueChange = { onEvent(ProfileEvent.UpdateProvince(it)) },
                    label = { Text("Province") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.provinceError != null,
                    supportingText = editState.provinceError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editState.postalCode,
                    onValueChange = { onEvent(ProfileEvent.UpdatePostalCode(it)) },
                    label = { Text("Postal Code") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = editState.postalCodeError != null,
                    supportingText = editState.postalCodeError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.sosho_blue),
                            focusedLabelColor = colorResource(id = R.color.sosho_blue),
                        ),
                )
            } else {
                user?.address?.let { address ->
                    ProfileInfoRow("Street", address.streetAddress, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Suburb", address.suburb, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("City", address.city, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Province", address.province, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Postal Code", address.postalCode, isDarkMode)
                } ?: Text(
                    text = "No address information available",
                    color = if (isDarkMode) Color.Gray else Color.LightGray,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun DocumentsSection(
    documentState: com.soshopay.android.ui.state.DocumentUploadState,
    onUploadNationalId: () -> Unit,
    onUploadProofOfResidence: () -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Documents",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // National ID
            DocumentItem(
                title = "National ID",
                document = documentState.nationalId,
                isUploading = documentState.isUploadingNationalId,
                progress = documentState.nationalIdProgress,
                onUpload = onUploadNationalId,
                isDarkMode = isDarkMode,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Proof of Residence
            DocumentItem(
                title = "Proof of Residence",
                document = documentState.proofOfResidence,
                isUploading = documentState.isUploadingProofOfResidence,
                progress = documentState.proofOfResidenceProgress,
                onUpload = onUploadProofOfResidence,
                isDarkMode = isDarkMode,
            )
        }
    }
}

@Composable
fun DocumentItem(
    title: String,
    document: com.soshopay.domain.model.Document?,
    isUploading: Boolean,
    progress: Float,
    onUpload: () -> Unit,
    isDarkMode: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color.White else Color.Black,
            )
            if (document != null) {
                Text(
                    text = "Uploaded • ${document.verificationStatus}",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.Gray else Color.LightGray,
                )
            } else {
                Text(
                    text = "Not uploaded",
                    fontSize = 12.sp,
                    color = Color.Red,
                )
            }
        }

        if (isUploading) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(40.dp),
                color = colorResource(id = R.color.sosho_blue),
            )
        } else {
            IconButton(onClick = onUpload) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = "Upload $title",
                    tint = colorResource(id = R.color.sosho_blue),
                )
            }
        }
    }
}

@Composable
fun NextOfKinSection(
    user: com.soshopay.domain.model.User?,
    editState: com.soshopay.android.ui.state.NextOfKinEditState,
    isEditMode: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Next of Kin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditMode) {
                user?.nextOfKin?.let { kin ->
                    ProfileInfoRow("Full Name", kin.fullName, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Relationship", kin.relationship, isDarkMode)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Phone Number", kin.phoneNumber, isDarkMode)
                } ?: Text(
                    text = "No next of kin information available",
                    color = if (isDarkMode) Color.Gray else Color.LightGray,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun ClientTypeSection(
    user: com.soshopay.domain.model.User?,
    onChangeClientType: () -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Client Type",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user?.clientType?.name ?: "Not set",
                        color = if (isDarkMode) Color.Gray else Color.LightGray,
                    )
                }

                TextButton(onClick = onChangeClientType) {
                    Text(
                        text = "Change",
                        color = colorResource(id = R.color.sosho_blue),
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    isDarkMode: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = if (isDarkMode) Color.Gray else Color.LightGray,
            fontSize = 14.sp,
        )
        Text(
            text = value,
            color = if (isDarkMode) Color.White else Color.Black,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text("Are you sure you want to logout?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.sosho_blue),
                    ),
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                )
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
    )
}

@Composable
fun ClientTypeDialog(
    availableTypes: List<String>,
    onSelectType: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Client Type",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                availableTypes.forEach { type ->
                    TextButton(
                        onClick = { onSelectType(type) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = type,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = if (isDarkMode) Color.White else colorResource(id = R.color.sosho_blue),
                )
            }
        },
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else Color.White,
    )
}
