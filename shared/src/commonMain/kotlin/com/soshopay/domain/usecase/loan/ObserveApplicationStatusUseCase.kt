package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class ObserveApplicationStatusUseCase(
    private val loanRepository: LoanRepository,
) {
    operator fun invoke(applicationId: String): Flow<ApplicationStatus> = loanRepository.observeApplicationStatus(applicationId)
}
