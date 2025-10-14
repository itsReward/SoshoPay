package com.soshopay.android.ui.state

import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanApplicationStep
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.CollateralDocument

/**
 * Enhanced UI State for Cash Loan Application with multi-step wizard support.
 *
 * Following SOLID principles:
 * - Single Responsibility: Manages only cash loan application UI state
 * - Open/Closed: Can be extended without modification
 * - Interface Segregation: Contains only necessary UI state properties
 *
 * @property formData Form metadata (min/max amounts, industries, etc.)
 * @property application The current application data
 * @property currentStep Current step in the wizard
 * @property loanAmount Loan amount as string (for text field binding)
 * @property loanPurpose Free text loan purpose
 * @property repaymentPeriod Selected repayment period
 * @property monthlyIncome Monthly income as string (for text field binding)
 * @property employerIndustry Selected employer industry
 * @property collateralType Type of collateral (free text)
 * @property collateralValue Collateral value as string (for text field binding)
 * @property collateralDetails Detailed description of collateral
 * @property collateralDocuments List of uploaded collateral documents
 * @property uploadingDocument Whether a document is currently being uploaded
 * @property uploadProgress Upload progress (0.0 to 1.0)
 * @property calculatedTerms Calculated loan terms (if available)
 * @property isLoading General loading state
 * @property isSaving Whether the application is being saved
 * @property isCalculating Whether loan terms are being calculated
 * @property errorMessage General error message
 * @property validationErrors Field-specific validation errors
 * @property showTermsDialog Whether to show the terms review dialog
 * @property showConfirmationDialog Whether to show the final confirmation dialog
 * @property lastAutoSaveTime Timestamp of last auto-save (for UI feedback)
 */
data class CashLoanApplicationState(
    // Form Metadata
    val formData: CashLoanFormData? = null,
    val application: CashLoanApplication? = null,
    // Wizard Navigation
    val currentStep: CashLoanApplicationStep = CashLoanApplicationStep.LOAN_DETAILS,
    // Step 1: Loan Details
    val loanAmount: String = "",
    val loanPurpose: String = "",
    val repaymentPeriod: String = "",
    // Step 2: Income & Employment
    val monthlyIncome: String = "",
    val employerIndustry: String = "",
    // Step 3: Collateral Information
    val collateralType: String = "",
    val collateralValue: String = "",
    val collateralDetails: String = "",
    val collateralDocuments: List<CollateralDocument> = emptyList(),
    val uploadingDocument: Boolean = false,
    val uploadProgress: Float = 0f,
    // Step 4 & 5: Terms & Confirmation
    val calculatedTerms: CashLoanTerms? = null,
    // Loading States
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCalculating: Boolean = false,
    // Error Handling
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    // Dialog States
    val showTermsDialog: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    // Auto-save Tracking
    val lastAutoSaveTime: Long? = null,
) {
    /**
     * Checks if there are any errors
     * @return true if errorMessage or validationErrors are present
     */
    fun hasErrors(): Boolean = errorMessage != null || validationErrors.isNotEmpty()

    /**
     * Checks if the current step is valid and complete
     * @return true if all required fields for the current step are filled correctly
     */
    fun isCurrentStepValid(): Boolean =
        when (currentStep) {
            CashLoanApplicationStep.LOAN_DETAILS -> isStep1Valid()
            CashLoanApplicationStep.INCOME_EMPLOYMENT -> isStep2Valid()
            CashLoanApplicationStep.COLLATERAL_INFO -> isStep3Valid()
            CashLoanApplicationStep.TERMS_REVIEW -> isStep4Valid()
            CashLoanApplicationStep.CONFIRMATION -> calculatedTerms != null
        }

    /**
     * Validates Step 1: Loan Details
     */
    private fun isStep1Valid(): Boolean {
        val amount = loanAmount.toDoubleOrNull() ?: 0.0
        return amount >= 100.0 &&
            amount <= 50000.0 &&
            loanPurpose.isNotBlank() &&
            repaymentPeriod.isNotBlank() &&
            !hasValidationErrorsForStep1()
    }

    /**
     * Validates Step 2: Income & Employment
     */
    private fun isStep2Valid(): Boolean {
        val income = monthlyIncome.toDoubleOrNull() ?: 0.0
        return income >= 150.0 &&
            employerIndustry.isNotBlank() &&
            !hasValidationErrorsForStep2()
    }

    /**
     * Validates Step 3: Collateral Information
     */
    private fun isStep3Valid(): Boolean {
        val value = collateralValue.toDoubleOrNull() ?: 0.0
        return collateralType.isNotBlank() &&
            value > 0.0 &&
            collateralDetails.isNotBlank() &&
            collateralDocuments.isNotEmpty() &&
            !hasValidationErrorsForStep3()
    }

    /**
     * Validates Step 4: Terms Review
     */
    private fun isStep4Valid(): Boolean = calculatedTerms != null

    /**
     * Checks for validation errors in Step 1 fields
     */
    private fun hasValidationErrorsForStep1(): Boolean =
        validationErrors.keys.any {
            it in listOf("loanAmount", "loanPurpose", "repaymentPeriod")
        }

    /**
     * Checks for validation errors in Step 2 fields
     */
    private fun hasValidationErrorsForStep2(): Boolean =
        validationErrors.keys.any {
            it in listOf("monthlyIncome", "employerIndustry")
        }

    /**
     * Checks for validation errors in Step 3 fields
     */
    private fun hasValidationErrorsForStep3(): Boolean =
        validationErrors.keys.any {
            it in listOf("collateralType", "collateralValue", "collateralDetails", "collateralDocuments")
        }

    /**
     * Checks if the "Next" button should be enabled
     * @return true if current step is valid and not loading
     */
    fun canProceedToNext(): Boolean = isCurrentStepValid() && !isLoading && !isSaving && !isCalculating

    /**
     * Checks if the "Calculate Terms" button should be enabled
     * @return true if Step 3 is complete and not already calculating
     */
    fun canCalculateTerms(): Boolean =
        isStep1Valid() &&
            isStep2Valid() &&
            isStep3Valid() &&
            !isCalculating

    /**
     * Checks if the application can be submitted
     * @return true if all steps are complete and terms are accepted
     */
    fun canSubmitApplication(): Boolean =
        isStep1Valid() &&
            isStep2Valid() &&
            isStep3Valid() &&
            isStep4Valid() &&
            !isLoading &&
            !isSaving

    /**
     * Gets the progress percentage for the wizard
     * @return Progress as a float between 0.0 and 1.0
     */
    fun getProgress(): Float = currentStep.getProgress()

    /**
     * Gets a user-friendly message about auto-save status
     * @return String describing when the draft was last saved, or null
     */
    fun getAutoSaveMessage(): String? =
        lastAutoSaveTime?.let {
            val now = System.currentTimeMillis()
            val diff = now - it
            val seconds = diff / 1000

            when {
                seconds < 10 -> "Saved just now"
                seconds < 60 -> "Saved $seconds seconds ago"
                else -> {
                    val minutes = seconds / 60
                    "Saved $minutes minute${if (minutes != 1L) "s" else ""} ago"
                }
            }
        }

    /**
     * Gets the number of uploaded collateral documents
     * @return Count of documents
     */
    fun getDocumentCount(): Int = collateralDocuments.size

    /**
     * Checks if any operation is in progress
     * @return true if loading, saving, calculating, or uploading
     */
    fun isAnyOperationInProgress(): Boolean = isLoading || isSaving || isCalculating || uploadingDocument
}
