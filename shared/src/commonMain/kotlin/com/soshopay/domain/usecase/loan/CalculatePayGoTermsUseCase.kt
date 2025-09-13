package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.PayGoCalculationRequest
import com.soshopay.domain.model.PayGoLoanTerms
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class CalculatePayGoTermsUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(
        productId: String,
        repaymentPeriod: String,
        usagePerDay: String,
        salaryBand: String,
        monthlyIncome: Double,
    ): Result<PayGoLoanTerms> {
        val request =
            PayGoCalculationRequest(
                productId = productId,
                repaymentPeriod = repaymentPeriod,
                usagePerDay = usagePerDay,
                salaryBand = salaryBand,
                monthlyIncome = monthlyIncome,
            )

        // Validate input
        val validation = validatePayGoRequest(request)
        if (!validation.isValid) {
            return Result.Error(Exception(validation.getErrorMessage()))
        }

        return loanRepository.calculatePayGoTerms(request)
    }

    private fun validatePayGoRequest(request: PayGoCalculationRequest): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.productId.isBlank()) {
            errors.add("Product selection is required")
        }

        if (request.repaymentPeriod.isBlank()) {
            errors.add("Repayment period is required")
        }

        if (request.usagePerDay.isBlank()) {
            errors.add("Usage per day is required")
        }

        if (request.salaryBand.isBlank()) {
            errors.add("Salary band is required")
        }

        if (request.monthlyIncome <= 0) {
            errors.add("Monthly income must be greater than zero")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }
}
