package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Clock

class SubmitCashLoanApplicationUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(application: CashLoanApplication): Result<String> {
        // Validate application
        val validation = loanRepository.validateCashLoanApplication(application)
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

        // Submit application
        return loanRepository.submitCashLoanApplication(
            application.copy(
                status = ApplicationStatus.SUBMITTED,
                submittedAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }
}
