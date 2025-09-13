package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class SyncLoansUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(): Result<Unit> = loanRepository.syncLoansFromRemote()
}
