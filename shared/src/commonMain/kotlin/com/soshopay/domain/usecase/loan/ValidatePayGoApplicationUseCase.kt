package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.LoanRepository

class ValidatePayGoApplicationUseCase(
    private val loanRepository: LoanRepository,
) {
    operator fun invoke(application: PayGoLoanApplication): ValidationResult = loanRepository.validatePayGoApplication(application)
}
