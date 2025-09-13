package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetLoanDetailsUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(loanId: String): Result<LoanDetails> {
        if (loanId.isBlank()) {
            return Result.Error(Exception("Loan ID is required"))
        }
        return loanRepository.getLoanDetails(loanId)
    }
}
