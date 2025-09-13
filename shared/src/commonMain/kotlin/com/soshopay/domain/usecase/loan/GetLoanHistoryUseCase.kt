package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.LoanHistoryFilter
import com.soshopay.domain.model.LoanHistoryResponse
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetLoanHistoryUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(
        filter: LoanHistoryFilter = LoanHistoryFilter.ALL,
        page: Int = 1,
        limit: Int = 20,
    ): Result<LoanHistoryResponse> {
        if (page < 1) {
            return Result.Error(Exception("Page must be greater than 0"))
        }

        if (limit < 1 || limit > 100) {
            return Result.Error(Exception("Limit must be between 1 and 100"))
        }

        return loanRepository.getLoanHistory(filter.value, page, limit)
    }
}
