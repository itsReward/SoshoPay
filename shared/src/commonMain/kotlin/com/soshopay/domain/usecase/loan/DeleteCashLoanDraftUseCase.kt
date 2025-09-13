package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class DeleteCashLoanDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(applicationId: String): Result<Unit> = loanRepository.deleteDraftCashLoanApplication(applicationId)
}
