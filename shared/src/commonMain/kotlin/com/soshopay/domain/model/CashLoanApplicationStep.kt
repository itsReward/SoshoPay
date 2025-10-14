package com.soshopay.domain.model

/**
 * Represents the different steps in the cash loan application wizard.
 *
 * Following the Single Responsibility Principle, this enum clearly defines
 * the progression through the loan application process.
 *
 * @property stepNumber The sequential step number (1-based)
 * @property title Display title for the step
 * @property canNavigateBack Whether user can navigate back from this step
 */
enum class CashLoanApplicationStep(
    val stepNumber: Int,
    val title: String,
    val canNavigateBack: Boolean = true,
) {
    LOAN_DETAILS(
        stepNumber = 1,
        title = "Loan Details",
        canNavigateBack = false, // First step, cannot go back
    ),
    INCOME_EMPLOYMENT(
        stepNumber = 2,
        title = "Income & Employment",
        canNavigateBack = true,
    ),
    COLLATERAL_INFO(
        stepNumber = 3,
        title = "Collateral Information",
        canNavigateBack = true,
    ),
    TERMS_REVIEW(
        stepNumber = 4,
        title = "Review Terms",
        canNavigateBack = true,
    ),
    CONFIRMATION(
        stepNumber = 5,
        title = "Confirmation",
        canNavigateBack = false, // Final step, user must accept or cancel
    ),
    ;

    /**
     * Gets the next step in the application flow
     * @return Next CashLoanApplicationStep or null if this is the last step
     */
    fun next(): CashLoanApplicationStep? = values().getOrNull(ordinal + 1)

    /**
     * Gets the previous step in the application flow
     * @return Previous CashLoanApplicationStep or null if this is the first step
     */
    fun previous(): CashLoanApplicationStep? = if (canNavigateBack) values().getOrNull(ordinal - 1) else null

    /**
     * Checks if this is the first step
     */
    fun isFirstStep(): Boolean = this == LOAN_DETAILS

    /**
     * Checks if this is the last step
     */
    fun isLastStep(): Boolean = this == CONFIRMATION

    /**
     * Gets the progress percentage for this step
     * @return Progress as a float between 0.0 and 1.0
     */
    fun getProgress(): Float = stepNumber.toFloat() / values().size.toFloat()

    companion object {
        /**
         * Gets a step by its step number
         * @param stepNumber The step number (1-based)
         * @return The corresponding CashLoanApplicationStep or null
         */
        fun fromStepNumber(stepNumber: Int): CashLoanApplicationStep? = values().find { it.stepNumber == stepNumber }

        /**
         * Gets the total number of steps
         */
        fun getTotalSteps(): Int = values().size
    }
}
