package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Clock

class SubmitPayGoApplicationUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(application: PayGoLoanApplication): Result<String> {
        // Validate application
        val validation = loanRepository.validatePayGoApplication(application)
        if (!validation.isValid) {
            return Result.Error(Exception(validation.getErrorMessage()))
        }

        // Ensure terms are calculated and accepted
        if (application.calculatedTerms == null) {
            return Result.Error(Exception("Please calculate loan terms before submitting"))
        }

        if (!application.acceptedTerms) {
            return Result.Error(Exception("You must accept the loan terms to proceed"))
        }

        // Validate guarantor information
        if (!application.guarantor.isComplete()) {
            return Result.Error(Exception("Complete guarantor information is required"))
        }

        // Submit application
        return loanRepository.submitPayGoApplication(
            application.copy(
                status = ApplicationStatus.SUBMITTED,
                submittedAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }
}
