package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class WithdrawLoanApplicationUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(applicationId: String): Result<Unit> {
        if (applicationId.isBlank()) {
            return Result.Error(Exception("Application ID is required"))
        }
        return loanRepository.withdrawApplication(applicationId)
    }
}
