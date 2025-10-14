package com.soshopay.android.ui.component.loans.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.soshopay.android.ui.component.loans.CollateralDocumentUploader
import com.soshopay.android.ui.state.LoanPaymentEvent
import com.soshopay.domain.model.CollateralDocument

/**
 * Step 3: Collateral Information Screen.
 *
 * Collects:
 * - Collateral type (free text)
 * - Collateral value
 * - Collateral details (description)
 * - Collateral documents/photos
 *
 * @param collateralType Current collateral type
 * @param collateralValue Current collateral value
 * @param collateralDetails Current collateral details
 * @param collateralDocuments List of uploaded documents
 * @param validationErrors Map of field validation errors
 * @param uploadingDocument Whether a document is being uploaded
 * @param uploadProgress Upload progress (0.0 to 1.0)
 * @param onEvent Callback for handling events
 * @param modifier Modifier for customization
 */
@Composable
fun CashLoanStep3Screen(
    collateralType: String,
    collateralValue: String,
    collateralDetails: String,
    collateralDocuments: List<CollateralDocument>,
    validationErrors: Map<String, String>,
    uploadingDocument: Boolean,
    uploadProgress: Float,
    onEvent: (LoanPaymentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = "Collateral Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Provide details about the collateral you're offering for this loan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Collateral Type
        OutlinedTextField(
            value = collateralType,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateCollateralType(it)) },
            label = { Text("Collateral Type") },
            placeholder = { Text("e.g., Vehicle, Property, Equipment, Jewelry") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Collateral",
                )
            },
            supportingText = {
                Text(
                    text =
                        validationErrors["collateralType"]
                            ?: "What type of collateral are you offering?",
                )
            },
            isError = validationErrors.containsKey("collateralType"),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Collateral Value
        OutlinedTextField(
            value = collateralValue,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateCollateralValue(it)) },
            label = { Text("Estimated Value") },
            placeholder = { Text("Enter estimated value") },
            leadingIcon = {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            },
            supportingText = {
                Text(
                    text =
                        validationErrors["collateralValue"]
                            ?: "Estimated market value of your collateral",
                )
            },
            isError = validationErrors.containsKey("collateralValue"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Collateral Details
        OutlinedTextField(
            value = collateralDetails,
            onValueChange = { onEvent(LoanPaymentEvent.UpdateCollateralDetails(it)) },
            label = { Text("Collateral Details") },
            placeholder = {
                Text("Provide detailed description (brand, model, condition, serial number, etc.)")
            },
            supportingText = {
                Text(
                    text =
                        validationErrors["collateralDetails"]
                            ?: "The more details, the better",
                )
            },
            isError = validationErrors.containsKey("collateralDetails"),
            minLines = 4,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
        )

        // Document Uploader
        Text(
            text = "Collateral Documents",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Upload photos or documents of your collateral (at least 1 required)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CollateralDocumentUploader(
            documents = collateralDocuments,
            onDocumentSelected = { bytes, name, type ->
                onEvent(LoanPaymentEvent.UploadCollateralDocument(bytes, name, type))
            },
            onDocumentRemoved = { documentId ->
                onEvent(LoanPaymentEvent.RemoveCollateralDocument(documentId))
            },
            isUploading = uploadingDocument,
            uploadProgress = uploadProgress,
            error = validationErrors["collateralDocuments"],
            modifier = Modifier.fillMaxWidth(),
        )

        // Info cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "📸 Photo Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text =
                        "• Take clear, well-lit photos\n" +
                            "• Capture all angles and identifying features\n" +
                            "• Include serial numbers if applicable\n" +
                            "• Show any damage or wear",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "⚠️ Important",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "While there's no minimum collateral-to-loan ratio, providing collateral with value close to or exceeding your loan amount may improve your approval chances and interest rate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
