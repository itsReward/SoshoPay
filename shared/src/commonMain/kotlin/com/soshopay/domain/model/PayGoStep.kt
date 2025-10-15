package com.soshopay.domain.model

/**
 * Represents the different steps in the PayGo loan application wizard.
 *
 * Following the Single Responsibility Principle, this enum clearly defines
 * the progression through the PayGo loan application process.
 *
 * @property stepNumber The sequential step number (1-based)
 * @property title Display title for the step
 * @property canNavigateBack Whether user can navigate back from this step
 */
enum class PayGoStep(
    val stepNumber: Int,
    val title: String,
    val canNavigateBack: Boolean = true,
) {
    CATEGORY_SELECTION(
        stepNumber = 1,
        title = "Category Selection",
        canNavigateBack = false, // First step, cannot go back
    ),
    PRODUCT_SELECTION(
        stepNumber = 2,
        title = "Product Selection",
        canNavigateBack = true,
    ),
    APPLICATION_DETAILS(
        stepNumber = 3,
        title = "Application Details",
        canNavigateBack = true,
    ),
    GUARANTOR_INFO(
        stepNumber = 4,
        title = "Guarantor Information",
        canNavigateBack = true,
    ),
    TERMS_REVIEW(
        stepNumber = 5,
        title = "Review Terms",
        canNavigateBack = false, // Final step, user must accept or cancel
    ),
    ;

    /**
     * Gets the next step in the application flow
     * @return Next PayGoStep or null if this is the last step
     */
    fun next(): PayGoStep? = values().getOrNull(ordinal + 1)

    /**
     * Gets the previous step in the application flow
     * @return Previous PayGoStep or null if this is the first step
     */
    fun previous(): PayGoStep? = if (canNavigateBack) values().getOrNull(ordinal - 1) else null

    /**
     * Checks if this is the first step
     */
    fun isFirstStep(): Boolean = this == CATEGORY_SELECTION

    /**
     * Checks if this is the last step
     */
    fun isLastStep(): Boolean = this == TERMS_REVIEW

    /**
     * Gets the progress percentage for this step
     * @return Progress as a float between 0.0 and 1.0
     */
    fun getProgress(): Float = stepNumber.toFloat() / values().size.toFloat()

    companion object {
        /**
         * Gets a step by its step number
         * @param stepNumber The step number (1-based)
         * @return The corresponding PayGoStep or null
         */
        fun fromStepNumber(stepNumber: Int): PayGoStep? = values().find { it.stepNumber == stepNumber }

        /**
         * Gets the total number of steps
         */
        fun getTotalSteps(): Int = values().size
    }
}
