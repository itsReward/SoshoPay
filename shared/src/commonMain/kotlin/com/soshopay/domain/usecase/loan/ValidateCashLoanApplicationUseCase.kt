package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.LoanRepository

class ValidateCashLoanApplicationUseCase(
    private val loanRepository: LoanRepository,
) {
    operator fun invoke(application: CashLoanApplication): ValidationResult = loanRepository.validateCashLoanApplication(application)
}
