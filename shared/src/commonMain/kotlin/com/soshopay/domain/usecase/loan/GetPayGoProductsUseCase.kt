package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoProduct
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class GetPayGoProductsUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(categoryId: String): Result<List<PayGoProduct>> {
        if (categoryId.isBlank()) {
            return Result.Error(Exception("Category ID is required"))
        }
        return loanRepository.getCategoryProducts(categoryId)
    }
}
