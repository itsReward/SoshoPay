// File: shared/src/commonMain/kotlin/com/soshopay/domain/usecase/loan/GetCashLoanFormDataUseCase.kt
package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetCashLoanFormDataUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(): Result<CashLoanFormData> = loanRepository.getCashLoanFormData()
}
