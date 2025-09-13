package com.soshopay.domain.usecase.loan

import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetPayGoCategoriesUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(): Result<List<String>> = loanRepository.getPayGoCategories()
}
