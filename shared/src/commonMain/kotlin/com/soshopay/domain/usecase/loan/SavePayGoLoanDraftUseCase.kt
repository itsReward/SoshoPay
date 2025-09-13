package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Clock

class SavePayGoLoanDraftUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(application: PayGoLoanApplication): Result<Unit> =
        loanRepository.saveDraftPayGoApplication(
            application.copy(
                status = ApplicationStatus.DRAFT,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
}
