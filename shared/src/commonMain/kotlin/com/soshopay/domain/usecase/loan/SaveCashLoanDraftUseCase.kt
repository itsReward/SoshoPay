package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Clock

class SaveCashLoanDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(application: CashLoanApplication): Result<Unit> =
        loanRepository.saveDraftCashLoanApplication(
            application.copy(
                status = ApplicationStatus.DRAFT,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
}
