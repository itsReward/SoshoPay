package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.Loan
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetCurrentLoansUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(): Result<List<Loan>> = loanRepository.getCurrentLoans()
}
