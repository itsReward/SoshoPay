package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.Loan
import com.soshopay.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class ObserveLoansUseCase(
    private val loanRepository: LoanRepository,
) {
    operator fun invoke(): Flow<List<Loan>> = loanRepository.observeLoanUpdates()
}
