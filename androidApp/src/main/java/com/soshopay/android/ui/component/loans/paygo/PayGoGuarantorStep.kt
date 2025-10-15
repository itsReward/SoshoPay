package com.soshopay.android.ui.component.loans.paygo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.common.FormTextField
import com.soshopay.domain.model.Address
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.Guarantor
import com.soshopay.domain.model.VerificationStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Step 4: Guarantor Information for PayGo Loan
 *
 * Collects complete guarantor information including personal details,
 * employment, and address information.
 *
 * Following Material Design 3 and SOLID principles.
 *
 * @param guarantor Current guarantor information
 * @param formData Form data for dropdown options
 * @param onGuarantorChange Callback when guarantor data changes
 * @param validationErrors Map of field validation errors
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun PayGoGuarantorStep(
    guarantor: Guarantor?,
    formData: CashLoanFormData?,
    onGuarantorChange: (Guarantor) -> Unit,
    validationErrors: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // Local state for form fields
    var name by remember { mutableStateOf(guarantor?.name ?: "") }
    var mobileNumber by remember { mutableStateOf(guarantor?.mobileNumber ?: "") }
    var nationalId by remember { mutableStateOf(guarantor?.nationalId ?: "") }
    var occupationClass by remember { mutableStateOf(guarantor?.occupationClass ?: "") }
    var monthlyIncome by remember { mutableStateOf(guarantor?.monthlyIncome?.toString() ?: "") }
    var relationship by remember { mutableStateOf(guarantor?.relationshipToClient ?: "") }

    // Address fields
    var streetAddress by remember { mutableStateOf(guarantor?.address?.streetAddress ?: "") }
    var suburb by remember { mutableStateOf(guarantor?.address?.suburb ?: "") }
    var city by remember { mutableStateOf(guarantor?.address?.city ?: "") }
    var province by remember { mutableStateOf(guarantor?.address?.province ?: "") }

    // Update guarantor when fields change
    LaunchedEffect(
        name,
        mobileNumber,
        nationalId,
        occupationClass,
        monthlyIncome,
        relationship,
        streetAddress,
        suburb,
        city,
        province,
    ) {
        if (name.isNotEmpty() || mobileNumber.isNotEmpty() || nationalId.isNotEmpty()) {
            val address =
                Address(
                    streetAddress = streetAddress,
                    suburb = suburb,
                    city = city,
                    province = province,
                    postalCode = "",
                    residenceType = "Residential",
                    lastUpdated = Clock.System.now().toEpochMilliseconds(),
                )

            val updatedGuarantor =
                Guarantor(
                    id = guarantor?.id ?: "",
                    applicationId = guarantor?.applicationId ?: "",
                    name = name,
                    mobileNumber = mobileNumber,
                    nationalId = nationalId,
                    occupationClass = occupationClass,
                    monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0,
                    relationshipToClient = relationship,
                    address = address,
                    verificationStatus =
                        guarantor?.verificationStatus
                            ?: VerificationStatus.PENDING,
                    createdAt = guarantor?.createdAt ?: System.currentTimeMillis(),
                )
            onGuarantorChange(updatedGuarantor)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
    ) {
        // Header
        Text(
            text = "Guarantor Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Provide details of someone who can guarantee your loan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Important notice about guarantor
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Important",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "About Guarantors",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A guarantor is someone who agrees to repay the loan if you're unable to. They should be a trusted person with stable income.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Information Section
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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Enter guarantor's full name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                        )
                    },
                    isError = validationErrors.containsKey("guarantorName"),
                    supportingText =
                        validationErrors["guarantorName"]?.let {
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile Number
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        if (it.length <= 13) mobileNumber = it
                    },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("+263...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = validationErrors.containsKey("guarantorMobile"),
                    supportingText =
                        validationErrors["guarantorMobile"]?.let {
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // National ID
                OutlinedTextField(
                    value = nationalId,
                    onValueChange = {
                        if (it.length <= 20) nationalId = it
                    },
                    label = { Text("National ID Number *") },
                    placeholder = { Text("Enter national ID") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                        )
                    },
                    isError = validationErrors.containsKey("guarantorNationalId"),
                    supportingText =
                        validationErrors["guarantorNationalId"]?.let {
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Relationship
                var relationshipExpanded by remember { mutableStateOf(false) }
                val relationships =
                    listOf(
                        "Spouse",
                        "Parent",
                        "Sibling",
                        "Child",
                        "Friend",
                        "Colleague",
                        "Business Partner",
                        "Other Family Member",
                    )

                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = !relationshipExpanded },
                ) {
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship to You *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("guarantorRelationship"),
                        supportingText = {
                            validationErrors["guarantorRelationship"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = relationshipExpanded,
                        onDismissRequest = { relationshipExpanded = false },
                    ) {
                        relationships.forEach { rel ->
                            DropdownMenuItem(
                                text = { Text(rel) },
                                onClick = {
                                    relationship = rel
                                    relationshipExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Employment Information Section
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
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Employment Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Occupation Class
                var occupationExpanded by remember { mutableStateOf(false) }
                val occupations =
                    listOf(
                        "Employed - Formal Sector",
                        "Employed - Informal Sector",
                        "Self-Employed",
                        "Business Owner",
                        "Farmer",
                        "Other",
                    )

                ExposedDropdownMenuBox(
                    expanded = occupationExpanded,
                    onExpandedChange = { occupationExpanded = !occupationExpanded },
                ) {
                    OutlinedTextField(
                        value = occupationClass,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Occupation Class *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = occupationExpanded)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("guarantorOccupation"),
                        supportingText = {
                            validationErrors["guarantorOccupation"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = occupationExpanded,
                        onDismissRequest = { occupationExpanded = false },
                    ) {
                        occupations.forEach { occ ->
                            DropdownMenuItem(
                                text = { Text(occ) },
                                onClick = {
                                    occupationClass = occ
                                    occupationExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Income
                FormTextField(
                    value = monthlyIncome,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            monthlyIncome = it
                        }
                    },
                    label = "Monthly Income (USD) *",
                    placeholder = "Enter monthly income",
                    leadingIcon = Icons.Default.AttachMoney,
                    keyboardType = KeyboardType.Decimal,
                    errorMessage = validationErrors["guarantorIncome"],
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Address Information Section
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
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Address Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Street Address
                FormTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = "Street Address *",
                    placeholder = "Enter street address",
                    leadingIcon = Icons.Default.LocationOn,
                    errorMessage = validationErrors["guarantorStreetAddress"],
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suburb
                FormTextField(
                    value = suburb,
                    onValueChange = { suburb = it },
                    label = "Suburb *",
                    placeholder = "Enter suburb",
                    leadingIcon = Icons.Default.LocationCity,
                    errorMessage = validationErrors["guarantorSuburb"],
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // City
                FormTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "City *",
                    placeholder = "Enter city",
                    leadingIcon = Icons.Default.LocationCity,
                    errorMessage = validationErrors["guarantorCity"],
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Province
                var provinceExpanded by remember { mutableStateOf(false) }
                val provinces =
                    listOf(
                        "Harare",
                        "Bulawayo",
                        "Manicaland",
                        "Mashonaland Central",
                        "Mashonaland East",
                        "Mashonaland West",
                        "Masvingo",
                        "Matabeleland North",
                        "Matabeleland South",
                        "Midlands",
                    )

                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = !provinceExpanded },
                ) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Province *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        isError = validationErrors.containsKey("guarantorProvince"),
                        supportingText = {
                            validationErrors["guarantorProvince"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false },
                    ) {
                        provinces.forEach { prov ->
                            DropdownMenuItem(
                                text = { Text(prov) },
                                onClick = {
                                    province = prov
                                    provinceExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
