package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

/**
 * Use Case for deleting PayGo loan application drafts
 */
class DeletePayGoDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(applicationId: String): Result<Unit> = loanRepository.deleteDraftPayGoApplication(applicationId)
}
