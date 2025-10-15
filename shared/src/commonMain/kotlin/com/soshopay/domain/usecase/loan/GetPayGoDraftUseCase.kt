package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

/**
 * Use Case for retrieving PayGo loan application drafts
 */
class GetPayGoDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(userId: String): Result<PayGoLoanApplication?> = loanRepository.getDraftPayGoApplication(userId)
}
