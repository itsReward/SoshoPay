package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetPayGoLoanDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(userId: String): Result<PayGoLoanApplication?> = loanRepository.getDraftPayGoApplication(userId)
}
