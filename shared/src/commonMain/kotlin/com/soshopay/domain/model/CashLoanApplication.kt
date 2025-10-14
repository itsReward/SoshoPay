package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Represents a cash loan application in the SoshoPay system.
 *
 * Enhanced with multi-step wizard support and collateral document management.
 *
 * Following SOLID principles:
 * - Single Responsibility: Manages cash loan application data only
 * - Open/Closed: Can be extended without modification
 * - Liskov Substitution: Can be used anywhere an application is expected
 *
 * @property id Unique identifier for the application
 * @property userId User ID who created the application
 * @property applicationId Application reference ID
 * @property loanType Type of loan (always CASH for this class)
 * @property loanAmount Requested loan amount in USD
 * @property repaymentPeriod Repayment period (e.g., "6 months", "1 year")
 * @property loanPurpose Purpose of the loan (free text)
 * @property employerIndustry Industry of the employer
 * @property monthlyIncome Monthly income of the applicant
 * @property collateralType Type of collateral (free text, e.g., "Vehicle", "Property")
 * @property collateralValue Estimated value of the collateral
 * @property collateralDetails Detailed description of the collateral
 * @property collateralDocuments List of documents/photos of the collateral
 * @property calculatedTerms Calculated loan terms (if calculated)
 * @property status Current status of the application
 * @property currentStep Current step in the application wizard
 * @property submittedAt Timestamp when application was submitted
 * @property reviewStartedAt Timestamp when review started
 * @property reviewCompletedAt Timestamp when review was completed
 * @property acceptedTerms Whether user accepted the loan terms
 * @property createdAt Timestamp when application was created
 * @property updatedAt Timestamp when application was last updated
 */
@Serializable
data class CashLoanApplication(
    val id: String = "",
    val userId: String = "",
    val applicationId: String = "",
    val loanType: LoanType = LoanType.CASH,
    // Step 1: Loan Details
    val loanAmount: Double = 0.0,
    val loanPurpose: String = "",
    val repaymentPeriod: String = "",
    // Step 2: Income & Employment
    val monthlyIncome: Double = 0.0,
    val employerIndustry: String = "",
    // Step 3: Collateral Information
    val collateralType: String = "",
    val collateralValue: Double = 0.0,
    val collateralDetails: String = "",
    val collateralDocuments: List<CollateralDocument> = emptyList(),
    // Step 4 & 5: Terms Review & Confirmation
    val calculatedTerms: CashLoanTerms? = null,
    val acceptedTerms: Boolean = false,
    // Application Status & Tracking
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val currentStep: CashLoanApplicationStep = CashLoanApplicationStep.LOAN_DETAILS,
    val submittedAt: Long = 0,
    val reviewStartedAt: Long? = null,
    val reviewCompletedAt: Long? = null,
    // Timestamps
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    /**
     * Checks if the application can be edited
     * @return true if the application is in DRAFT status
     */
    fun isEditable(): Boolean = status == ApplicationStatus.DRAFT

    /**
     * Checks if the application can be withdrawn
     * @return true if the application is SUBMITTED or UNDER_REVIEW
     */
    fun canBeWithdrawn(): Boolean =
        status in
            listOf(
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_REVIEW,
            )

    /**
     * Gets the user-friendly status text
     * @return Human-readable status string
     */
    fun getStatusText(): String =
        when (status) {
            ApplicationStatus.DRAFT -> "Draft"
            ApplicationStatus.SUBMITTED -> "Submitted"
            ApplicationStatus.UNDER_REVIEW -> "Under Review"
            ApplicationStatus.APPROVED -> "Approved"
            ApplicationStatus.REJECTED -> "Rejected"
            ApplicationStatus.CANCELLED -> "Cancelled"
        }

    /**
     * Checks if Step 1 (Loan Details) is complete
     * @return true if all required fields are filled
     */
    fun isStep1Complete(): Boolean =
        loanAmount > 0 &&
            loanPurpose.isNotBlank() &&
            repaymentPeriod.isNotBlank()

    /**
     * Checks if Step 2 (Income & Employment) is complete
     * @return true if all required fields are filled
     */
    fun isStep2Complete(): Boolean =
        monthlyIncome > 0 &&
            employerIndustry.isNotBlank()

    /**
     * Checks if Step 3 (Collateral Information) is complete
     * @return true if all required fields are filled and at least one document is uploaded
     */
    fun isStep3Complete(): Boolean =
        collateralType.isNotBlank() &&
            collateralValue > 0 &&
            collateralDetails.isNotBlank() &&
            collateralDocuments.isNotEmpty()

    /**
     * Checks if Step 4 (Terms Review) is complete
     * @return true if terms have been calculated
     */
    fun isStep4Complete(): Boolean = calculatedTerms != null

    /**
     * Checks if the application can move to the next step
     * @return true if the current step is complete
     */
    fun canProceedToNextStep(): Boolean =
        when (currentStep) {
            CashLoanApplicationStep.LOAN_DETAILS -> isStep1Complete()
            CashLoanApplicationStep.INCOME_EMPLOYMENT -> isStep2Complete()
            CashLoanApplicationStep.COLLATERAL_INFO -> isStep3Complete()
            CashLoanApplicationStep.TERMS_REVIEW -> isStep4Complete()
            CashLoanApplicationStep.CONFIRMATION -> acceptedTerms
        }

    /**
     * Gets the overall completion percentage
     * @return Progress as a float between 0.0 and 1.0
     */
    fun getCompletionProgress(): Float {
        val completedSteps =
            listOf(
                isStep1Complete(),
                isStep2Complete(),
                isStep3Complete(),
                isStep4Complete(),
                acceptedTerms,
            ).count { it }

        return completedSteps.toFloat() / 5f
    }

    /**
     * Gets the number of uploaded collateral documents
     * @return Count of documents
     */
    fun getCollateralDocumentCount(): Int = collateralDocuments.size

    /**
     * Checks if collateral documents are sufficient
     * @return true if at least one document is uploaded
     */
    fun hasCollateralDocuments(): Boolean = collateralDocuments.isNotEmpty()

    /**
     * Validates the application for the current step
     * @return ValidationResult with any errors for the current step
     */
    fun validateCurrentStep(): ValidationResult {
        val errors = mutableListOf<String>()

        when (currentStep) {
            CashLoanApplicationStep.LOAN_DETAILS -> {
                if (loanAmount <= 0) errors.add("Loan amount is required")
                if (loanAmount < 100) errors.add("Minimum loan amount is $100")
                if (loanAmount > 50000) errors.add("Maximum loan amount is $50,000")
                if (loanPurpose.isBlank()) errors.add("Loan purpose is required")
                if (repaymentPeriod.isBlank()) errors.add("Repayment period is required")
            }
            CashLoanApplicationStep.INCOME_EMPLOYMENT -> {
                if (monthlyIncome <= 0) errors.add("Monthly income is required")
                if (monthlyIncome < 150) errors.add("Minimum monthly income is $150")
                if (employerIndustry.isBlank()) errors.add("Employer industry is required")
            }
            CashLoanApplicationStep.COLLATERAL_INFO -> {
                if (collateralType.isBlank()) errors.add("Collateral type is required")
                if (collateralValue <= 0) errors.add("Collateral value is required")
                if (collateralDetails.isBlank()) errors.add("Collateral details are required")
                if (collateralDocuments.isEmpty()) errors.add("At least one collateral document is required")
            }
            CashLoanApplicationStep.TERMS_REVIEW -> {
                if (calculatedTerms == null) errors.add("Loan terms must be calculated")
            }
            CashLoanApplicationStep.CONFIRMATION -> {
                if (!acceptedTerms) errors.add("You must accept the loan terms to proceed")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }

    companion object {
        /**
         * Minimum loan amount in USD
         */
        const val MIN_LOAN_AMOUNT = 100.0

        /**
         * Maximum loan amount in USD
         */
        const val MAX_LOAN_AMOUNT = 50000.0

        /**
         * Minimum monthly income in USD
         */
        const val MIN_MONTHLY_INCOME = 150.0

        /**
         * Creates an empty draft application
         * @param userId The user ID
         * @return A new CashLoanApplication in DRAFT status
         */
        fun createDraft(userId: String): CashLoanApplication =
            CashLoanApplication(
                userId = userId,
                status = ApplicationStatus.DRAFT,
                currentStep = CashLoanApplicationStep.LOAN_DETAILS,
            )
    }
}
