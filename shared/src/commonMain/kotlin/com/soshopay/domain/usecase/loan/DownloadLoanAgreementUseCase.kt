package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class DownloadLoanAgreementUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(loanId: String): Result<ByteArray> {
        if (loanId.isBlank()) {
            return Result.Error(Exception("Loan ID is required"))
        }
        return loanRepository.downloadLoanAgreement(loanId)
    }
}
