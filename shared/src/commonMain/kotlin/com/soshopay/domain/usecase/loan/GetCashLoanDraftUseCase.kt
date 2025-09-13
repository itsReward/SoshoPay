package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetCashLoanDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(userId: String): Result<CashLoanApplication?> = loanRepository.getDraftCashLoanApplication(userId)
}
