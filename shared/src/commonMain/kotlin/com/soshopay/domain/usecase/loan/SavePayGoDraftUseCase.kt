package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

/**
 * Use Case for saving PayGo loan application drafts
 *
 * Following SOLID principles:
 * - Single Responsibility: Handles only draft saving
 * - Dependency Inversion: Depends on LoanRepository abstraction
 */
class SavePayGoDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(application: PayGoLoanApplication): Result<Unit> = loanRepository.saveDraftPayGoApplication(application)
}
